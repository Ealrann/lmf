package org.logoce.lmf.generator.model;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.EnumAttribute;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Primitive;
import org.logoce.lmf.generator.code.feature.MethodUtil;

import javax.lang.model.element.Modifier;
import java.io.File;

public class EnumGenerator
{
	private final Enum<?> enumeration;
	private final String packageName;

	public EnumGenerator(Enum<?> enumeration)
	{
		this.enumeration = enumeration;
		final var model = (MetaModel) enumeration.lmContainer();
		packageName = TargetPathUtil.packageName(model);
	}

	public void generate(final File target)
	{
		final var enumBuilder = TypeSpec.enumBuilder(enumeration.name())
										.addModifiers(Modifier.PUBLIC);

		final var attributes = enumeration.attributes();
		if (attributes.isEmpty())
		{
			for (final var literal : enumeration.literals())
			{
				final var constantName = GenUtils.capitalizeFirstLetter(extractLiteralName(literal));
				enumBuilder.addEnumConstant(constantName);
			}
		}
		else
		{
			final var attributeSpecs = attributes.stream()
												 .map(EnumAttributeSpec::of)
												 .toList();

			attributeSpecs.forEach(spec -> enumBuilder.addField(FieldSpec.builder(spec.type(),
																				 spec.javaName(),
																				 Modifier.PRIVATE,
																				 Modifier.FINAL)
																   .build()));

			final var constructor = MethodSpec.constructorBuilder();
			attributeSpecs.forEach(spec -> constructor.addParameter(spec.type(),
																	spec.javaName(),
																	Modifier.FINAL));
			attributeSpecs.forEach(spec -> constructor.addStatement("this.$N = $N", spec.javaName(), spec.javaName()));
			enumBuilder.addMethod(constructor.build());

			attributeSpecs.forEach(spec -> enumBuilder.addMethod(MethodSpec.methodBuilder(spec.javaName())
																		.addModifiers(Modifier.PUBLIC)
																		.returns(spec.type())
																		.addStatement("return $N", spec.javaName())
																		.build()));

			final var parsedLiterals = parseAttributedLiterals(enumeration.literals(), attributeSpecs.size());
			for (final var literal : parsedLiterals)
			{
				final var constantName = GenUtils.capitalizeFirstLetter(literal.name());
				final var args = zipArguments(attributeSpecs, literal)
						.collect(CodeBlock.joining(", "));

				enumBuilder.addEnumConstant(constantName, TypeSpec.anonymousClassBuilder("$L", args).build());
			}
		}

		final var javaFile = JavaFile.builder(packageName, enumBuilder.build())
									 .skipJavaLangImports(true)
									 .build();
		FormattedJavaWriter.write(javaFile, target);
	}

	private static String extractLiteralName(final String raw)
	{
		if (raw == null)
		{
			return "";
		}

		final var colonIndex = raw.indexOf(':');
		if (colonIndex < 0)
		{
			return raw;
		}
		return raw.substring(0, colonIndex);
	}

	private record EnumAttributeSpec(String javaName, TypeName type, Primitive primitive)
	{
		static EnumAttributeSpec of(final EnumAttribute attribute)
		{
			if (attribute == null)
			{
				throw new IllegalArgumentException("EnumAttribute cannot be null");
			}

			final var unit = attribute.unit();
			if (unit == null)
			{
				throw new IllegalStateException("EnumAttribute \"" + attribute.name() + "\" has no unit");
			}

			final var primitive = unit.primitive();
			final var primitiveType = TypeName.get(GenUtils.resolvePrimitiveClass(primitive));
			final var javaName = MethodUtil.validateParameterName(attribute.name());

			return new EnumAttributeSpec(javaName, primitiveType, primitive);
		}
	}

	private record ParsedAttributedLiteral(String name, List<String> rawValues) {}

	private static List<ParsedAttributedLiteral> parseAttributedLiterals(final List<String> literals,
																		 final int attributeCount)
	{
		if (literals == null || literals.isEmpty())
		{
			return List.of();
		}
		if (attributeCount <= 0)
		{
			throw new IllegalArgumentException("attributeCount must be > 0");
		}

		final var arity = 1 + attributeCount;
		if (literals.size() % arity != 0)
		{
			return literals.stream().map(literal -> parseAttributedLiteral(literal, attributeCount)).toList();
		}

		final var parsed = new ArrayList<ParsedAttributedLiteral>(literals.size() / arity);
		for (int i = 0; i < literals.size(); i += arity)
		{
			final var name = literals.get(i).trim();
			final var rawValues = List.copyOf(literals.subList(i + 1, i + arity));
			parsed.add(new ParsedAttributedLiteral(name, rawValues));
		}
		return List.copyOf(parsed);
	}

	private static ParsedAttributedLiteral parseAttributedLiteral(final String raw,
																  final int attributeCount)
	{
		if (raw == null || raw.isBlank())
		{
			throw new IllegalArgumentException("Enum literal cannot be null/blank");
		}

		final var parts = splitColonValues(raw);
		if (parts.size() < 2)
		{
			throw new IllegalStateException("Enum literal \"" + raw + "\" has no attribute values");
		}

		final var literalName = parts.getFirst().trim();
		final var values = List.copyOf(parts.subList(1, parts.size()));

		if (values.size() != attributeCount)
		{
			throw new IllegalStateException("Enum literal \"" + raw + "\" has " + values.size() +
											" values but " + attributeCount + " attributes are declared");
		}

		return new ParsedAttributedLiteral(literalName, values);
	}

	private static List<String> splitColonValues(final String raw)
	{
		final var parts = new ArrayList<String>();
		final var current = new StringBuilder();
		boolean inQuotes = false;
		boolean escaped = false;

		for (int i = 0; i < raw.length(); i++)
		{
			final var ch = raw.charAt(i);
			if (escaped)
			{
				current.append(ch);
				escaped = false;
				continue;
			}

			if (inQuotes && ch == '\\')
			{
				escaped = true;
				continue;
			}

			if (ch == '"')
			{
				inQuotes = !inQuotes;
				continue;
			}

			if (ch == ':' && !inQuotes)
			{
				parts.add(current.toString());
				current.setLength(0);
				continue;
			}

			current.append(ch);
		}

		if (inQuotes)
		{
			throw new IllegalStateException("Unclosed quote in enum literal \"" + raw + '"');
		}

		parts.add(current.toString());
		return parts;
	}

	private static java.util.stream.Stream<CodeBlock> zipArguments(final List<EnumAttributeSpec> attributes,
																   final ParsedAttributedLiteral literal)
	{
		return java.util.stream.IntStream.range(0, attributes.size())
										 .mapToObj(i -> codeLiteral(attributes.get(i), literal.rawValues().get(i)));
	}

	private static CodeBlock codeLiteral(final EnumAttributeSpec attribute, final String rawValue)
	{
		return switch (attribute.primitive())
		{
			case String -> CodeBlock.of("$S", rawValue == null ? "" : rawValue);
			case Boolean ->
			{
				final var value = rawValue == null ? "" : rawValue.trim();
				if ("true".equalsIgnoreCase(value)) yield CodeBlock.of("true");
				if ("false".equalsIgnoreCase(value)) yield CodeBlock.of("false");
				throw new IllegalStateException("Invalid boolean literal \"" + value + '"');
			}
			case Float ->
			{
				final var value = rawValue == null ? "" : rawValue.trim();
				if (value.isEmpty())
				{
					throw new IllegalStateException("Empty value for enum attribute \"" + attribute.javaName() + '"');
				}
				yield CodeBlock.of("$L", normalizeFloatLiteral(value));
			}
			default ->
			{
				final var value = rawValue == null ? "" : rawValue.trim();
				if (value.isEmpty())
				{
					throw new IllegalStateException("Empty value for enum attribute \"" + attribute.javaName() + '"');
				}
				yield CodeBlock.of("$L", value);
			}
		};
	}

	private static String normalizeFloatLiteral(final String value)
	{
		final var trimmed = value.trim();
		final var endsWithF = trimmed.endsWith("f") || trimmed.endsWith("F");
		if (endsWithF)
		{
			return trimmed;
		}

		final var endsWithD = trimmed.endsWith("d") || trimmed.endsWith("D");
		final var base = endsWithD ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
		final var looksDecimal = base.contains(".") || base.contains("e") || base.contains("E");
		return looksDecimal ? base + 'f' : base;
	}
}
