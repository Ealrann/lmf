package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.impl.EnumImpl;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.Arrays;

public final class EnumFieldBuilder implements CodeBuilder<Enum<?>, FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
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
									 "\t\t\t\t\t\t\t\t$T.stream(" +
									 name +
									 ".values())\n" +
									 "\t\t\t\t\t\t\t\t\t\t.map(java.lang.Enum::name)\n" +
									 "\t\t\t\t\t\t\t\t\t\t.toList())", ENUM_IMPL_TYPE, name, ARRAYS_TYPE)
						.build();
	}
}
