package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.builder.EnumBuilder;

public final class EnumFieldBuilder implements DefinitionFieldBuilder<Enum<?>>
{
	public static final ClassName ENUM_TYPE = ClassName.get(Enum.class);
	public static final ClassName ENUM_BUILDER_TYPE = ClassName.get(EnumBuilder.class);

	@Override
	public FieldSpec build(Enum<?> input)
	{
		final var name = input.name();
		final var primitiveClassName = ClassName.get("", name);
		final var typedEnum = TypeParameter.of(ENUM_TYPE, primitiveClassName);
		final var literals = input.literals();
		final var initializerBuilder = CodeBlock.builder()
											   .add("new $T<$T>()", ENUM_BUILDER_TYPE, primitiveClassName)
											   .add(".name($S)", name);

		literals.forEach(literal -> initializerBuilder.add(".addLiteral($S)", literal));
		initializerBuilder.add(".build()");

		final var constantName = GenUtils.toConstantCase(name);

		return FieldSpec.builder(typedEnum.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}
}
