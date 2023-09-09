package isotropy.lmf.generator.util;

import com.squareup.javapoet.CodeBlock;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.generator.code.feature.FeatureResolution;

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
		}
		return Optional.empty();
	}
}
