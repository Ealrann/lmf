package org.logoce.lmf.generator.util;

import com.squareup.javapoet.CodeBlock;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Unit;

import java.util.Optional;

public class DefaultValueUtil
{
	public static Optional<CodeBlock> resolveDefaultValue(final FeatureResolution resolution)
	{
		final var attribute = (Attribute<?, ?>) resolution.feature();
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
		}
		return Optional.empty();
	}
}
