package org.logoce.lmf.cli.insert;

import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
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

final class InsertReferenceShiftPlanner
{
	void planDocumentEdits(final Path path,
						   final CharSequence source,
						   final List<LinkNodeInternal<?, PNode, ?>> linkRoots,
						   final InsertShiftContext shiftContext,
						   final Map<Path, List<TextEdits.TextEdit>> editsByFile,
						   final List<ReferenceRewrite> rewrites)
	{
		for (final var root : linkRoots)
		{
			root.streamTree().forEach(node -> planNodeEdits(path, node, source, shiftContext, editsByFile, rewrites));
		}
	}

	void planDocumentEdits(final org.logoce.lmf.cli.workspace.WorkspaceModelDocument doc,
						   final InsertShiftContext shiftContext,
						   final Map<Path, List<TextEdits.TextEdit>> editsByFile,
						   final List<ReferenceRewrite> rewrites)
	{
		final var document = doc.document();
		final var linkRoots = RootReferenceResolver.collectLinkRoots(document.linkTrees());
		planDocumentEdits(doc.path(), document.source(), linkRoots, shiftContext, editsByFile, rewrites);
	}

	private void planNodeEdits(final Path path,
							   final LinkNodeInternal<?, PNode, ?> node,
							   final CharSequence source,
							   final InsertShiftContext shiftContext,
							   final Map<Path, List<TextEdits.TextEdit>> editsByFile,
							   final List<ReferenceRewrite> rewrites)
	{
		final var attempts = node.relationResolutions();
		if (attempts == null || attempts.isEmpty())
		{
			return;
		}

		final var index = FeatureValueSpanIndex.build(node.pNode().tokens(), source);
		final var queuesByFeature = new HashMap<FeatureValueSpanIndex.FeatureSpan, Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>>>();
		Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>> positionalQueues = null;

		for (final ResolutionAttempt<Relation<?, ?, ?, ?>> attempt : attempts)
		{
			final var relation = resolveRelation(attempt);
			if (relation == null)
			{
				continue;
			}

			final var featureSpan = resolveFeatureSpan(index, relation.name());
			final Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>> valueQueues;
			if (featureSpan != null)
			{
				valueQueues = queuesByFeature.computeIfAbsent(featureSpan, InsertReferenceShiftPlanner::buildQueues);
			}
			else
			{
				if (positionalQueues == null)
				{
					positionalQueues = buildPositionalQueues(index);
				}
				valueQueues = positionalQueues;
			}
			for (final var resolved : RelationReferences.resolved(attempt))
			{
				final var raw = resolved.raw();
				if (raw == null)
				{
					continue;
				}

				final FeatureValueSpanIndex.ValueSpan valueSpan;
				if (featureSpan != null)
				{
					valueSpan = pollValueSpan(valueQueues, featureSpan, raw);
				}
				else
				{
					valueSpan = pollValueSpan(valueQueues, raw);
				}
				if (valueSpan == null)
				{
					throw new InsertPlanException("Cannot locate value span for '" + raw + "'");
				}

				final var targetId = ObjectId.from(resolved.target());
				if (targetId == null)
				{
					continue;
				}

				if (!shiftContext.shouldShift(targetId, raw))
				{
					continue;
				}

				final var indexInPath = shiftContext.extractIndex(targetId.path());
				if (indexInPath.isEmpty())
				{
					continue;
				}

				final var updated = shiftContext.shiftRaw(raw, indexInPath.getAsInt());
				if (Objects.equals(updated, raw))
				{
					continue;
				}

				final var edits = editsByFile.computeIfAbsent(path, ignored -> new ArrayList<>());
				edits.add(new TextEdits.TextEdit(valueSpan.span().offset(), valueSpan.span().length(), updated));
				rewrites.add(new ReferenceRewrite(path, valueSpan.span(), raw, updated, targetId));
			}
		}
	}

	private static FeatureValueSpanIndex.FeatureSpan resolveFeatureSpan(final FeatureValueSpanIndex index, final String featureName)
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

	private static FeatureValueSpanIndex.ValueSpan pollValueSpan(final Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>> queues,
																final String raw)
	{
		final var queue = queues.get(raw);
		if (queue == null || queue.isEmpty())
		{
			return null;
		}
		return queue.removeFirst();
	}

	private static Map<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>> buildPositionalQueues(final FeatureValueSpanIndex index)
	{
		final var queues = new HashMap<String, ArrayDeque<FeatureValueSpanIndex.ValueSpan>>();
		for (final var feature : index.features())
		{
			if (feature.name() != null)
			{
				continue;
			}
			for (final var value : feature.values())
			{
				queues.computeIfAbsent(value.raw(), ignored -> new ArrayDeque<>()).add(value);
			}
		}
		return queues;
	}
}
