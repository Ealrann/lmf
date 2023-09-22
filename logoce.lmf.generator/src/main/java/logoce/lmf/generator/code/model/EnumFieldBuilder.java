package logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import logoce.lmf.generator.util.GenUtils;
import logoce.lmf.generator.util.TypeParameter;
import logoce.lmf.model.lang.impl.EnumImpl;
import logoce.lmf.model.lang.Enum;

import java.util.Arrays;

public final class EnumFieldBuilder implements DefinitionFieldBuilder<Enum<?>>
{
	public static final ClassName ENUM_TYPE = ClassName.get(Enum.class);
	public static final ClassName ENUM_IMPL_TYPE = ClassName.get(EnumImpl.class);
	public static final ClassName ARRAYS_TYPE = ClassName.get(Arrays.class);

	@Override
	public FieldSpec build(Enum<?> input)
	{
		final var name = input.name();
		final var primitiveClassName = ClassName.get("", name);
		final var typedEnum = TypeParameter.of(ENUM_TYPE, primitiveClassName);

		final var constantName = GenUtils.toConstantCase(name);

		return FieldSpec.builder(typedEnum.parametrized(), constantName, modifiers)
						.initializer("new $T<>($S,\n" +
									 "$T.stream(" +
									 name +
									 ".values())\n" +
									 ".map(java.lang.Enum::name)\n" +
									 ".toList())", ENUM_IMPL_TYPE, name, ARRAYS_TYPE)
						.build();
	}
}
