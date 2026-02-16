package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.RelationReferences;
import org.logoce.lmf.core.loader.api.loader.linking.ResolutionAttempt;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ReferenceEditPlanner
{
	@FunctionalInterface
	public interface ReferenceMapper
	{
		String map(LinkNodeInternal<?, PNode, ?> node, String raw, ObjectId targetId, boolean quoted);
	}

	@FunctionalInterface
	public interface RewriteListener
	{
		void onRewrite(TextPositions.Span span, String oldRaw, String newRaw, ObjectId resolvedTarget);
	}

	public List<TextEdits.TextEdit> planEdits(final CharSequence source,
									   final List<LinkNodeInternal<?, PNode, ?>> roots,
									   final Set<LinkNodeInternal<?, PNode, ?>> skipNodes,
									   final ReferenceMapper mapper,
									   final int offsetAdjustment)
	{
		return planEdits(source, roots, skipNodes, mapper, offsetAdjustment, null);
	}

	public List<TextEdits.TextEdit> planEdits(final CharSequence source,
											 final List<LinkNodeInternal<?, PNode, ?>> roots,
											 final Set<LinkNodeInternal<?, PNode, ?>> skipNodes,
											 final ReferenceMapper mapper,
											 final int offsetAdjustment,
											 final RewriteListener rewriteListener)
	{
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(roots, "roots");
		Objects.requireNonNull(mapper, "mapper");

		final var edits = new ArrayList<TextEdits.TextEdit>();
		final var skip = skipNodes == null ? Set.<LinkNodeInternal<?, PNode, ?>>of() : skipNodes;

		for (final var root : roots)
		{
			root.streamTree()
				.forEach(node -> {
					if (skip.contains(node))
					{
						return;
					}
					planNodeEdits(node, source, mapper, offsetAdjustment, rewriteListener, edits);
				});
		}

		return List.copyOf(edits);
	}

	private static void planNodeEdits(final LinkNodeInternal<?, PNode, ?> node,
								 final CharSequence source,
								 final ReferenceMapper mapper,
								 final int offsetAdjustment,
								 final RewriteListener rewriteListener,
								 final List<TextEdits.TextEdit> edits)
	{
		final var attempts = node.relationResolutions();
		if (attempts == null || attempts.isEmpty())
		{
			return;
		}

		final var pNode = node.pNode();
		if (pNode == null)
		{
			return;
		}

		final var index = FeatureValueSpanIndex.build(pNode.tokens(), source);
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
				valueQueues = queuesByFeature.computeIfAbsent(featureSpan, ReferenceEditPlanner::buildQueues);
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
					throw new ReferenceEditException("Cannot locate value span for '" + raw + "'");
				}

				final var targetId = ObjectId.from(resolved.target());
				if (targetId == null)
				{
					continue;
				}

				final var updated = mapper.map(node, raw, targetId, valueSpan.quoted());
				if (updated == null || updated.equals(raw))
				{
					continue;
				}

				final int adjustedOffset = valueSpan.span().offset() + offsetAdjustment;
				if (adjustedOffset < 0)
				{
					throw new ReferenceEditException("Reference edit offset is negative after adjustment");
				}
				if (rewriteListener != null)
				{
					rewriteListener.onRewrite(valueSpan.span(), raw, updated, targetId);
				}
				edits.add(new TextEdits.TextEdit(adjustedOffset, valueSpan.span().length(), updated));
			}
		}
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
																 final String raw)
	{
		final var queue = queues.get(raw);
		if (queue == null || queue.isEmpty())
		{
			return null;
		}
		return queue.removeFirst();
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

	public static final class ReferenceEditException extends RuntimeException
	{
		public ReferenceEditException(final String message)
		{
			super(message);
		}
	}
}
