package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.format.RootReferenceResolver;
import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.RelationReferences;
import org.logoce.lmf.core.loader.api.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class RemoveDocumentPlanner
{
	private final RemoveMutationApplier mutationApplier = new RemoveMutationApplier();

	void planDocumentEdits(final RemoveModelDocument doc,
						   final Set<ObjectId> removedIds,
						   final RemoveShiftContext shiftContext,
						   final Set<LinkNodeInternal<?, PNode, ?>> skipNodes,
						   final Map<Path, List<TextEdits.TextEdit>> editsByFile,
						   final List<RemoveUnsetReference> unsets)
	{
		final var document = doc.document();
		final var source = document.source();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());

		for (final var root : linkRoots)
		{
			root.streamTree().forEach(node -> {
				if (skipNodes.contains(node))
				{
					return;
				}
				planNodeEdits(doc.path(),
							  node,
							  source,
							  removedIds,
							  shiftContext,
							  editsByFile,
							  unsets);
			});
		}
	}

	private void planNodeEdits(final Path path,
							   final LinkNodeInternal<?, PNode, ?> node,
							   final CharSequence source,
							   final Set<ObjectId> removedIds,
							   final RemoveShiftContext shiftContext,
							   final Map<Path, List<TextEdits.TextEdit>> editsByFile,
							   final List<RemoveUnsetReference> unsets)
	{
		final var attempts = node.relationResolutions();
		if (attempts == null || attempts.isEmpty())
		{
			return;
		}

		final var index = FeatureValueSpanIndex.build(node.pNode().tokens(), source);
		final var mutationsByFeature = new HashMap<FeatureValueSpanIndex.FeatureSpan, List<RemoveValueMutation>>();
		final var queuesByFeature = new HashMap<FeatureValueSpanIndex.FeatureSpan, Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>>>();

		for (final ResolutionAttempt<Relation<?, ?, ?, ?>> attempt : attempts)
		{
			final var relation = resolveRelation(attempt);
			if (relation == null)
			{
				continue;
			}

			final var featureSpan = resolveFeatureSpan(index, relation.name());
			if (featureSpan == null)
			{
				throw new RemovePlanException("Cannot locate feature span for '" + relation.name() + "'");
			}

			final var valueQueues = queuesByFeature.computeIfAbsent(featureSpan, RemoveDocumentPlanner::buildQueues);
			for (final var resolved : RelationReferences.resolved(attempt))
			{
				final var raw = resolved.raw();
				if (raw == null)
				{
					continue;
				}

				final var valueSpan = pollValueSpan(valueQueues, featureSpan, raw);
				if (valueSpan == null)
				{
					throw new RemovePlanException("Cannot locate value span for '" + raw + "'");
				}

				final var targetId = ObjectId.from(resolved.target());
				if (targetId == null)
				{
					continue;
				}

				if (removedIds.contains(targetId))
				{
					mutationsByFeature.computeIfAbsent(featureSpan, ignored -> new ArrayList<>())
									  .add(new RemoveValueMutation(RemoveMutationKind.REMOVE, valueSpan, null));
					unsets.add(new RemoveUnsetReference(path, valueSpan.span(), raw, targetId));
					continue;
				}

				if (shiftContext != null && shiftContext.shouldShift(targetId, raw))
				{
					final var indexInPath = shiftContext.extractIndex(targetId.path());
					if (indexInPath.isPresent())
					{
						final var updated = shiftContext.shiftRaw(raw, indexInPath.getAsInt());
						if (!updated.equals(raw))
						{
							mutationsByFeature.computeIfAbsent(featureSpan, ignored -> new ArrayList<>())
											  .add(new RemoveValueMutation(RemoveMutationKind.SHIFT,
																		   valueSpan,
																		   updated));
						}
					}
				}
			}
		}

		if (mutationsByFeature.isEmpty())
		{
			return;
		}

		final var edits = editsByFile.computeIfAbsent(path, ignored -> new ArrayList<>());
		for (final var entry : mutationsByFeature.entrySet())
		{
			final var edit = mutationApplier.apply(source, entry.getKey(), entry.getValue());
			if (edit != null)
			{
				edits.add(edit);
			}
		}
	}

	private static FeatureValueSpanIndex.FeatureSpan resolveFeatureSpan(final FeatureValueSpanIndex index,
																		final String featureName)
	{
		final var candidates = index.featuresByName(featureName);
		if (!candidates.isEmpty())
		{
			return candidates.getFirst();
		}
		return null;
	}

	private static Relation<?, ?, ?, ?> resolveRelation(final ResolutionAttempt<Relation<?, ?, ?, ?>> attempt)
	{
		final var resolution = attempt.resolution();
		if (resolution == null)
		{
			return null;
		}

		final var feature = resolution.feature();
		if (feature instanceof Relation<?, ?, ?, ?> relation)
		{
			return relation;
		}
		return null;
	}

	private static Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>> buildQueues(final FeatureValueSpanIndex.FeatureSpan featureSpan)
	{
		final var queues = new HashMap<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>>();
		for (final var value : featureSpan.values())
		{
			queues.computeIfAbsent(value.raw(), ignored -> new ArrayDeque<>()).add(value);
		}
		return queues;
	}

	private static FeatureValueSpanIndex.ValueSpan pollValueSpan(final Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>> queues,
																final FeatureValueSpanIndex.FeatureSpan featureSpan,
																final String raw)
	{
		final var queue = queues.get(raw);
		if (queue != null && !queue.isEmpty())
		{
			return queue.removeFirst();
		}

		for (final var value : featureSpan.values())
		{
			if (Objects.equals(value.raw(), raw))
			{
				return value;
			}
		}
		return null;
	}
}
