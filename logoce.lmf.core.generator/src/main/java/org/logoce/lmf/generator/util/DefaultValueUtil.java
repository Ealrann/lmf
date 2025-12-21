package org.logoce.lmf.generator.util;

import com.squareup.javapoet.CodeBlock;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.JavaWrapper;
import org.logoce.lmf.core.lang.Primitive;
import org.logoce.lmf.core.lang.Unit;

import java.util.Optional;
import java.util.function.Function;

public class DefaultValueUtil
{
	public static Optional<CodeBlock> resolveDefaultValue(final FeatureResolution resolution)
	{
		final var attribute = (Attribute<?, ?, ?, ?>) resolution.feature();
		final var defaultValue = attribute.defaultValue();
		final var dataType = attribute.datatype();

		if (defaultValue != null)
		{
			if (dataType instanceof Enum<?>)
			{
				return Optional.of(CodeBlock.of("$T.$N", resolution.singleType().raw(), defaultValue));
			}
			else if (dataType instanceof Unit<?> unit)
			{
				if (unit.primitive() == Primitive.String) return Optional.of(CodeBlock.of("$S", defaultValue));
				else return Optional.of(CodeBlock.of("$L", defaultValue));
			}
			else if (dataType instanceof JavaWrapper<?> wrapper)
			{
				final var serializer = wrapper.serializer();
				if (serializer != null && serializer.create() != null)
				{
					final var javaType = resolution.singleType().parametrized();
					return Optional.of(CodeBlock.of("(($T<$T, $T>) it -> { $L }).apply($S)",
												 Function.class,
												 String.class,
												 javaType,
												 serializer.create(),
												 defaultValue));
				}
			}
		}
		else if (dataType instanceof Enum<?> enumeration)
		{
			final var literalName = firstEnumLiteralName(enumeration);
			if (literalName != null && !literalName.isBlank())
			{
				return Optional.of(CodeBlock.of("$T.$N", resolution.singleType().raw(), literalName));
			}
		}
		return Optional.empty();
	}

	private static String firstEnumLiteralName(final Enum<?> enumeration)
	{
		if (enumeration == null) return null;
		final var literals = enumeration.literals();
		if (literals == null || literals.isEmpty()) return null;

		final var raw = literals.get(0);
		if (raw == null) return null;

		final var colonIndex = raw.indexOf(':');
		if (colonIndex >= 0)
		{
			return raw.substring(0, colonIndex).trim();
		}
		return raw.trim();
	}
}
