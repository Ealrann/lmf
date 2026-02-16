package org.logoce.lmf.core.loader.api.tooling.validation;

import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.linking.LinkNode;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class LmConstraints
{
	private LmConstraints()
	{
	}

	/**
	 * Produces warning diagnostics for missing mandatory ([1..1] / [1..*]) features.
	 * <p>
	 * This is intentionally a tooling-layer check (not enforced by the loader), so that
	 * workflows can keep models temporarily incomplete and only treat those as warnings.
	 */
	public static List<LmDiagnostic> mandatoryFeatureWarnings(final List<? extends LinkNode<?, PNode>> linkTrees,
															 final CharSequence source)
	{
		Objects.requireNonNull(linkTrees, "linkTrees");
		Objects.requireNonNull(source, "source");

		if (linkTrees.isEmpty())
		{
			return List.of();
		}

		final var roots = new ArrayList<LinkNodeInternal<?, PNode, ?>>();
		for (final var node : linkTrees)
		{
			if (node instanceof LinkNodeInternal<?, PNode, ?> internal)
			{
				roots.add(internal);
			}
		}

		if (roots.isEmpty())
		{
			return List.of();
		}

		final var diagnostics = new ArrayList<LmDiagnostic>();
		for (final var root : roots)
		{
			collectMandatoryWarnings(root, "", source, diagnostics);
		}
		return List.copyOf(diagnostics);
	}

	private static void collectMandatoryWarnings(final LinkNodeInternal<?, PNode, ?> node,
												 final String nodePath,
												 final CharSequence source,
												 final List<LmDiagnostic> out)
	{
		final var group = node.group();
		if (group != null)
		{
			final var built = node.build();
			if (built != null)
			{
				checkMandatoryFeatures(node, built, group, nodePath, source, out);
			}
		}

		final var children = node.streamChildren()
								 .filter(child -> child.containingRelation() != null)
								 .toList();
		if (children.isEmpty())
		{
			return;
		}

		final var counts = new HashMap<String, Integer>();
		for (final var child : children)
		{
			final var relationName = child.containingRelation().name();
			counts.merge(relationName, 1, Integer::sum);
		}

		final var indices = new HashMap<String, Integer>();
		for (final var child : children)
		{
			final var relationName = child.containingRelation().name();
			final int index = indices.getOrDefault(relationName, 0);
			indices.put(relationName, index + 1);

			final boolean indexed = counts.getOrDefault(relationName, 0) > 1;
			final var segment = indexed ? relationName + "." + index : relationName;
			final var childPath = nodePath.isEmpty() ? "/" + segment : nodePath + "/" + segment;
			collectMandatoryWarnings(child, childPath, source, out);
		}
	}

	private static void checkMandatoryFeatures(final LinkNodeInternal<?, PNode, ?> node,
											  final LMObject built,
											  final Group<?> group,
											  final String nodePath,
											  final CharSequence source,
											  final List<LmDiagnostic> out)
	{
		final var seenIds = new HashSet<Integer>();
		ModelUtil.streamAllFeatures(group)
				 .filter(Feature::mandatory)
				 .filter(feature -> seenIds.add(feature.id()))
				 .forEach(feature -> {
					 final var value = built.get(feature);
					 if (missingMandatoryValue(feature, value))
					 {
						 out.add(missingMandatoryFeature(node, feature, group, nodePath, source));
					 }
				 });
	}

	private static boolean missingMandatoryValue(final Feature<?, ?, ?, ?> feature, final Object value)
	{
		if (feature.many())
		{
			if (value == null)
			{
				return true;
			}
			if (value instanceof List<?> list)
			{
				return list.isEmpty();
			}
			return true;
		}

		return value == null;
	}

	private static LmDiagnostic missingMandatoryFeature(final LinkNodeInternal<?, PNode, ?> node,
													   final Feature<?, ?, ?, ?> feature,
													   final Group<?> group,
													   final String nodePath,
													   final CharSequence source)
	{
		final TextPositions.Span span;
		try
		{
			span = TextPositions.spanOf(node.pNode(), source);
		}
		catch (RuntimeException ignored)
		{
			return new LmDiagnostic(1,
									1,
									1,
									0,
									LmDiagnostic.Severity.WARNING,
									missingMandatoryFeatureMessage(feature, group, nodePath));
		}

		return new LmDiagnostic(span.line(),
								span.column(),
								span.length(),
								span.offset(),
								LmDiagnostic.Severity.WARNING,
								missingMandatoryFeatureMessage(feature, group, nodePath));
	}

	private static String missingMandatoryFeatureMessage(final Feature<?, ?, ?, ?> feature,
														 final Group<?> group,
														 final String nodePath)
	{
		final var multiplicity = "[" + (feature.mandatory() ? "1" : "0") + ".." + (feature.many() ? "*" : "1") + "]";
		final var displayPath = nodePath == null || nodePath.isBlank() ? "/" : nodePath;
		return "Missing mandatory feature " + feature.name() + " (" + multiplicity + ") on " + group.name() + " at " + displayPath;
	}
}

