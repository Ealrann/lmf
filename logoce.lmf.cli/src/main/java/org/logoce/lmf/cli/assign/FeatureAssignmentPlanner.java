package org.logoce.lmf.cli.assign;

import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.util.LmStringLiteral;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

import java.util.List;
import java.util.Objects;

public final class FeatureAssignmentPlanner
{
	public TextEdits.TextEdit planSet(final LinkNodeInternal<?, PNode, ?> node,
									  final CharSequence source,
									  final String featureName,
									  final String userValue)
	{
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(featureName, "featureName");
		Objects.requireNonNull(userValue, "userValue");

		final var feature = resolveFeature(node.group(), featureName);
		final var value = formatValue(feature, userValue);
		final var assignment = featureName + "=" + value;

		final var index = FeatureValueSpanIndex.build(node.pNode().tokens(), source);
		final var spans = index.featuresByName(featureName);
		if (spans.size() > 1)
		{
			throw new AssignmentPlanningException("Multiple assignments found for feature '" + featureName + "'");
		}
		if (!spans.isEmpty())
		{
			final var span = spans.getFirst().assignmentSpan();
			return new TextEdits.TextEdit(span.offset(), span.length(), assignment);
		}

		final int insertionOffset = endOffsetOfLastNonWhitespaceToken(node.pNode().tokens());
		return new TextEdits.TextEdit(insertionOffset, 0, " " + assignment);
	}

	public TextEdits.TextEdit planUnset(final LinkNodeInternal<?, PNode, ?> node,
										final CharSequence source,
										final String featureName)
	{
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(featureName, "featureName");

		final var feature = resolveFeature(node.group(), featureName);
		if (feature.mandatory())
		{
			throw new AssignmentPlanningException("Cannot unset mandatory feature '" + featureName + "'");
		}
		if (feature.many())
		{
			throw new AssignmentPlanningException("Cannot unset list feature '" + featureName + "' (not supported yet)");
		}

		final var index = FeatureValueSpanIndex.build(node.pNode().tokens(), source);
		final var spans = index.featuresByName(featureName);
		if (spans.size() > 1)
		{
			throw new AssignmentPlanningException("Multiple assignments found for feature '" + featureName + "'");
		}
		if (spans.isEmpty())
		{
			return null;
		}

		final var span = spans.getFirst().assignmentSpan();
		return new TextEdits.TextEdit(span.offset(), span.length(), "");
	}

	private static Feature<?, ?, ?, ?> resolveFeature(final Group<?> group, final String featureName)
	{
		if (group == null)
		{
			throw new AssignmentPlanningException("Target object has no resolved group (cannot resolve feature '" + featureName + "')");
		}

		final var matches = ModelUtil.streamAllFeatures(group)
									 .filter(feature -> featureName.equals(feature.name()))
									 .toList();
		if (matches.isEmpty())
		{
			throw new AssignmentPlanningException("Unknown feature '" + featureName + "' for group '" + group.name() + "'");
		}
		if (matches.size() > 1)
		{
			throw new AssignmentPlanningException("Ambiguous feature '" + featureName + "' for group '" + group.name() + "'");
		}

		final var feature = matches.getFirst();
		if (feature instanceof Relation<?, ?, ?, ?> relation && relation.contains())
		{
			throw new AssignmentPlanningException("Feature '" + featureName + "' is a containment; use insert/replace/remove instead");
		}
		if (feature.many())
		{
			throw new AssignmentPlanningException("List feature '" + featureName + "' is not supported yet");
		}

		return feature;
	}

	private static String formatValue(final Feature<?, ?, ?, ?> feature, final String userValue)
	{
		if (feature instanceof Attribute<?, ?, ?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			final var datatypeName = datatype != null ? datatype.name() : null;
			if (datatypeName != null && datatypeName.equalsIgnoreCase("string"))
			{
				return LmStringLiteral.fromUserValue(userValue);
			}
			final var trimmed = userValue.strip();
			if (trimmed.isEmpty())
			{
				throw new AssignmentPlanningException("Empty value is not allowed for attribute '" + feature.name() + "'");
			}
			return trimmed;
		}

		final var trimmed = userValue.strip();
		if (trimmed.isEmpty())
		{
			throw new AssignmentPlanningException("Empty value is not allowed for feature '" + feature.name() + "'");
		}
		return trimmed;
	}

	private static int endOffsetOfLastNonWhitespaceToken(final List<PToken> tokens)
	{
		if (tokens == null || tokens.isEmpty())
		{
			throw new AssignmentPlanningException("Cannot locate insertion point (missing tokens)");
		}

		for (int i = tokens.size() - 1; i >= 0; i--)
		{
			final var token = tokens.get(i);
			if (token.type() == ELMTokenType.WHITE_SPACE)
			{
				continue;
			}
			return token.offset() + Math.max(0, token.length());
		}

		throw new AssignmentPlanningException("Cannot locate insertion point (all tokens are whitespace)");
	}

	private static final class AssignmentPlanningException extends RuntimeException
	{
		private AssignmentPlanningException(final String message)
		{
			super(message);
		}
	}
}
