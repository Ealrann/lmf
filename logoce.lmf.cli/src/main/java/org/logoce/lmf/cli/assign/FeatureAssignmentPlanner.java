package org.logoce.lmf.cli.assign;

import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.cli.edit.TextEdits;
import org.logoce.lmf.cli.util.LmStringLiteral;
import org.logoce.lmf.core.api.util.ModelUtil;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.lexer.ELMTokenType;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;

import java.util.ArrayList;
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
		final var assignment = planAssignment(feature, featureName, userValue);

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

	public TextEdits.TextEdit planAdd(final LinkNodeInternal<?, PNode, ?> node,
									  final CharSequence source,
									  final String featureName,
									  final String userValue)
	{
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(featureName, "featureName");
		Objects.requireNonNull(userValue, "userValue");

		final var feature = resolveFeature(node.group(), featureName);
		if (!feature.many())
		{
			throw new AssignmentPlanningException("Feature '" + featureName + "' is not a list; use set instead");
		}

		final var normalized = normalizeSingleValue(feature, userValue);

		final var index = FeatureValueSpanIndex.build(node.pNode().tokens(), source);
		final var spans = index.featuresByName(featureName);
		if (spans.size() > 1)
		{
			throw new AssignmentPlanningException("Multiple assignments found for feature '" + featureName + "'");
		}
		if (spans.isEmpty())
		{
			final int insertionOffset = endOffsetOfLastNonWhitespaceToken(node.pNode().tokens());
			final var assignment = featureName + "=" + normalized.formatted();
			return new TextEdits.TextEdit(insertionOffset, 0, " " + assignment);
		}

		final var featureSpan = spans.getFirst();
		final var values = featureSpan.values();
		if (values.stream().anyMatch(v -> Objects.equals(v.raw(), normalized.raw())))
		{
			return null;
		}

			final var updatedValues = new ArrayList<String>(values.size() + 1);
			for (final var valueSpan : values)
			{
				updatedValues.add(formatRawValueLiteral(valueSpan.raw()));
			}
			updatedValues.add(normalized.formatted());

		final var assignment = featureName + "=" + String.join(",", updatedValues);
		final var span = featureSpan.assignmentSpan();
		return new TextEdits.TextEdit(span.offset(), span.length(), assignment);
	}

	public TextEdits.TextEdit planRemoveValue(final LinkNodeInternal<?, PNode, ?> node,
											 final CharSequence source,
											 final String featureName,
											 final String userValue)
	{
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(featureName, "featureName");
		Objects.requireNonNull(userValue, "userValue");

		final var feature = resolveFeature(node.group(), featureName);
		if (!feature.many())
		{
			throw new AssignmentPlanningException("Feature '" + featureName + "' is not a list; use unset or set instead");
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

		final var featureSpan = spans.getFirst();
		final var values = featureSpan.values();
		final var normalized = normalizeSingleValue(feature, userValue);

		int removeIndex = -1;
		for (int i = 0; i < values.size(); i++)
		{
			if (Objects.equals(values.get(i).raw(), normalized.raw()))
			{
				removeIndex = i;
				break;
			}
		}
		if (removeIndex < 0)
		{
			return null;
		}

		if (values.size() == 1)
		{
			if (feature.mandatory())
			{
				throw new AssignmentPlanningException("Cannot remove the last value of mandatory list feature '" + featureName + "'");
			}
			final var span = featureSpan.assignmentSpan();
			return new TextEdits.TextEdit(span.offset(), span.length(), "");
		}

		final var updatedValues = new ArrayList<String>(values.size() - 1);
		for (int i = 0; i < values.size(); i++)
		{
			if (i == removeIndex)
			{
				continue;
			}
			updatedValues.add(formatRawValueLiteral(values.get(i).raw()));
		}

		final var assignment = featureName + "=" + String.join(",", updatedValues);
		final var span = featureSpan.assignmentSpan();
		return new TextEdits.TextEdit(span.offset(), span.length(), assignment);
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

	public TextEdits.TextEdit planClearList(final LinkNodeInternal<?, PNode, ?> node,
											final CharSequence source,
											final String featureName)
	{
		Objects.requireNonNull(node, "node");
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(featureName, "featureName");

		final var feature = resolveFeature(node.group(), featureName);
		if (!feature.many())
		{
			throw new AssignmentPlanningException("Feature '" + featureName + "' is not a list; use unset instead");
		}
		return planUnset(node, source, featureName);
	}

	private static Feature<?, ?, ?, ?> resolveFeature(final Group<?> group, final String featureName)
	{
		if (group == null)
		{
			throw new AssignmentPlanningException("Target object has no resolved group (cannot resolve feature '" + featureName + "')");
		}

		final var matches = collapseEquivalentFeatures(ModelUtil.streamAllFeatures(group)
																.filter(feature -> featureName.equals(feature.name()))
																.toList());
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
			throw new AssignmentPlanningException("Feature '" + featureName + "' is a containment; use insert/move/remove/replace instead");
		}

		return feature;
	}

	private static List<Feature<?, ?, ?, ?>> collapseEquivalentFeatures(final List<Feature<?, ?, ?, ?>> matches)
	{
		if (matches == null || matches.size() < 2)
		{
			return matches == null ? List.of() : matches;
		}

		final var unique = new ArrayList<Feature<?, ?, ?, ?>>(matches.size());
		final var seen = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Feature<?, ?, ?, ?>, Boolean>());
		for (final var feature : matches)
		{
			if (seen.add(feature))
			{
				unique.add(feature);
			}
		}
		if (unique.size() < 2)
		{
			return List.copyOf(unique);
		}

		final var first = unique.getFirst();
		for (int i = 1; i < unique.size(); i++)
		{
			if (!equivalentFeature(first, unique.get(i)))
			{
				return List.copyOf(unique);
			}
		}

		return List.of(first);
	}

	private static boolean equivalentFeature(final Feature<?, ?, ?, ?> left, final Feature<?, ?, ?, ?> right)
	{
		if (left == right)
		{
			return true;
		}
		if (left == null || right == null)
		{
			return false;
		}
		if (left.id() != right.id())
		{
			return false;
		}
		if (left.many() != right.many() || left.mandatory() != right.mandatory() || left.immutable() != right.immutable())
		{
			return false;
		}

		if (left instanceof Attribute<?, ?, ?, ?> leftAttribute)
		{
			if (!(right instanceof Attribute<?, ?, ?, ?> rightAttribute))
			{
				return false;
			}
			final var leftDatatype = leftAttribute.datatype();
			final var rightDatatype = rightAttribute.datatype();
			return Objects.equals(leftDatatype == null ? null : leftDatatype.name(),
								  rightDatatype == null ? null : rightDatatype.name());
		}

		if (left instanceof Relation<?, ?, ?, ?> leftRelation)
		{
			if (!(right instanceof Relation<?, ?, ?, ?> rightRelation))
			{
				return false;
			}
			if (leftRelation.contains() != rightRelation.contains())
			{
				return false;
			}
			final var leftConcept = leftRelation.concept();
			final var rightConcept = rightRelation.concept();
			return Objects.equals(leftConcept == null ? null : leftConcept.name(),
								  rightConcept == null ? null : rightConcept.name());
		}

		return right.getClass() == left.getClass();
	}

	private static String planAssignment(final Feature<?, ?, ?, ?> feature,
										 final String featureName,
										 final String userValue)
	{
		if (feature.many())
		{
			final var values = splitListValues(userValue);
			if (values.isEmpty())
			{
				throw new AssignmentPlanningException("Empty value is not allowed for list feature '" + featureName + "'");
			}

			final var formatted = values.stream()
										.map(value -> normalizeSingleValue(feature, value).formatted())
										.toList();
			return featureName + "=" + String.join(",", formatted);
		}

		final var normalized = normalizeSingleValue(feature, userValue);
		return featureName + "=" + normalized.formatted();
	}

	private static NormalizedSingleValue normalizeSingleValue(final Feature<?, ?, ?, ?> feature, final String userValue)
	{
		final var literal = candidateValueLiteral(feature, userValue);
		final var raw = parseSingleRawValue(feature.name(), literal);

		if (feature instanceof Attribute<?, ?, ?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			final var datatypeName = datatype != null ? datatype.name() : null;
			if (datatypeName != null && datatypeName.equalsIgnoreCase("string"))
			{
				return new NormalizedSingleValue(raw, formatRawValueLiteral(raw));
			}

			if (raw.isBlank())
			{
				throw new AssignmentPlanningException("Empty value is not allowed for attribute '" + feature.name() + "'");
			}
			if (containsWhitespace(raw))
			{
				throw new AssignmentPlanningException("Value contains whitespace; only string attributes can contain whitespace");
			}
			return new NormalizedSingleValue(raw, formatRawValueLiteral(raw));
		}

		if (feature instanceof Relation<?, ?, ?, ?>)
		{
			if (raw.isBlank())
			{
				throw new AssignmentPlanningException("Empty value is not allowed for relation '" + feature.name() + "'");
			}
			return new NormalizedSingleValue(raw, formatRawValueLiteral(raw));
		}

		if (raw.isBlank())
		{
			throw new AssignmentPlanningException("Empty value is not allowed for feature '" + feature.name() + "'");
		}
		if (containsWhitespace(raw))
		{
			throw new AssignmentPlanningException("Value contains whitespace; only string attributes can contain whitespace");
		}
		return new NormalizedSingleValue(raw, formatRawValueLiteral(raw));
	}

	private static String candidateValueLiteral(final Feature<?, ?, ?, ?> feature, final String userValue)
	{
		Objects.requireNonNull(feature, "feature");
		Objects.requireNonNull(userValue, "userValue");

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
			if (containsWhitespace(trimmed))
			{
				throw new AssignmentPlanningException("Value contains whitespace; only string attributes can contain whitespace");
			}
			return trimmed;
		}

		final var trimmed = userValue.strip();
		if (trimmed.isEmpty())
		{
			throw new AssignmentPlanningException("Empty value is not allowed for feature '" + feature.name() + "'");
		}

		if (feature instanceof Relation<?, ?, ?, ?> && !isQuotedLiteral(trimmed) && containsWhitespace(trimmed))
		{
			return "\"" + LmStringLiteral.escape(trimmed) + "\"";
		}

		if (containsWhitespace(trimmed))
		{
			throw new AssignmentPlanningException("Value contains whitespace; only string attributes can contain whitespace");
		}

		return trimmed;
	}

	private static boolean isQuotedLiteral(final String value)
	{
		return value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"");
	}

	private static String formatRawValueLiteral(final String raw)
	{
		if (raw == null)
		{
			return "\"\"";
		}
		return LmStringLiteral.needsQuotes(raw)
			   ? "\"" + LmStringLiteral.escape(raw) + "\""
			   : raw;
	}

	private static String parseSingleRawValue(final String featureName, final String valueLiteral)
	{
		final var snippet = "(X " + featureName + "=" + valueLiteral + ")";
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var readResult = new LmTreeReader().read(snippet, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics) || readResult.roots().isEmpty())
		{
			throw new AssignmentPlanningException("Invalid value");
		}

		final var root = readResult.roots().getFirst().data();
		final var index = FeatureValueSpanIndex.build(root.tokens(), snippet);
		final var features = index.features();
		if (features.size() != 1)
		{
			throw new AssignmentPlanningException("Invalid value");
		}

		final var only = features.getFirst();
		if (!Objects.equals(only.name(), featureName))
		{
			throw new AssignmentPlanningException("Invalid value");
		}

		final var values = only.values();
		if (values.size() != 1)
		{
			throw new AssignmentPlanningException("Value must be a single element; use set to replace the full list");
		}

		return values.getFirst().raw();
	}

	private static List<String> splitListValues(final String raw)
	{
		final var input = raw == null ? "" : raw.strip();
		if (input.isEmpty())
		{
			return List.of();
		}

		final var values = new ArrayList<String>();
		final var current = new StringBuilder();
		boolean inQuote = false;
		boolean escaped = false;

		for (int i = 0; i < input.length(); i++)
		{
			final char c = input.charAt(i);
			if (escaped)
			{
				current.append(c);
				escaped = false;
				continue;
			}
			if (c == '\\')
			{
				current.append(c);
				if (inQuote)
				{
					escaped = true;
				}
				continue;
			}
			if (c == '"')
			{
				inQuote = !inQuote;
				current.append(c);
				continue;
			}
			if (c == ',' && !inQuote)
			{
				addValue(values, current);
				current.setLength(0);
				continue;
			}
			current.append(c);
		}

		addValue(values, current);
		return List.copyOf(values);
	}

	private static void addValue(final List<String> out, final StringBuilder current)
	{
		final var value = current.toString().strip();
		if (value.isEmpty())
		{
			throw new AssignmentPlanningException("Empty list value is not allowed (use \"\" for an empty string)");
		}
		out.add(value);
	}

	private static boolean containsWhitespace(final String value)
	{
		for (int i = 0; i < value.length(); i++)
		{
			if (Character.isWhitespace(value.charAt(i)))
			{
				return true;
			}
		}
		return false;
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

	private record NormalizedSingleValue(String raw, String formatted)
	{
	}
}
