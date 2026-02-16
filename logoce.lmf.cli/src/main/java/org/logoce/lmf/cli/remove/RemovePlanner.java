package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.edit.SubtreeSpanLocator;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class RemovePlanner
{
	public RemovePlanResult plan(final RemoveWorkspace workspace, final String targetReference)
	{
		Objects.requireNonNull(workspace, "workspace");
		Objects.requireNonNull(targetReference, "targetReference");

		try
		{
			final var targetDocument = workspace.targetDocument();
			final var targetNode = new RemoveTargetResolver().resolve(targetDocument.document(), targetReference);

			final var nodeCollector = new RemoveNodeCollector();
			final var removedId = nodeCollector.resolveObjectId(targetNode, "target object");
			final var removedNodes = nodeCollector.collectNodes(targetNode);
			final var removedIds = nodeCollector.collectObjectIds(removedNodes);

			final var shiftContext = RemoveShiftContext.from(targetNode, removedId);

			final var editsByFile = new HashMap<Path, List<TextEdits.TextEdit>>();
			final var unsets = new ArrayList<RemoveUnsetReference>();
			final var rewrites = new ArrayList<ReferenceRewrite>();

			final var documentPlanner = new RemoveDocumentPlanner();
			for (final var doc : workspace.documents())
			{
				final var skipNodes = doc == targetDocument
									  ? removedNodes
									  : Set.<LinkNodeInternal<?, PNode, ?>>of();
				documentPlanner.planDocumentEdits(doc,
												  removedIds,
												  shiftContext,
												  skipNodes,
												  editsByFile,
												  unsets,
												  rewrites);
			}

			final var targetSource = targetDocument.document().source();
			final var subtreeSpan = SubtreeSpanLocator.locate(targetSource, targetNode);
			if (subtreeSpan == null)
			{
				throw new RemovePlanException("Cannot locate subtree span for " + targetReference);
			}

			editsByFile.computeIfAbsent(targetDocument.path(), ignored -> new ArrayList<>())
					   .add(new TextEdits.TextEdit(subtreeSpan.startOffset(), subtreeSpan.length(), ""));

			final var planned = new RemovePlannedEdit(copyEdits(editsByFile),
													  List.copyOf(unsets),
													  List.copyOf(rewrites),
													  removedId);
			return new RemovePlanResult.Success(planned);
		}
		catch (RemovePlanException failure)
		{
			return new RemovePlanResult.Failure(failure.getMessage());
		}
	}

	private static Map<Path, List<TextEdits.TextEdit>> copyEdits(final Map<Path, List<TextEdits.TextEdit>> editsByFile)
	{
		final var out = new HashMap<Path, List<TextEdits.TextEdit>>();
		for (final var entry : editsByFile.entrySet())
		{
			out.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return Map.copyOf(out);
	}
}
