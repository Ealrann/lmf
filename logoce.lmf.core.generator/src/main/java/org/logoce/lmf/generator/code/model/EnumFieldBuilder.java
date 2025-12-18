package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.generator.util.TypeResolutionUtil;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.EnumAttribute;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.builder.EnumAttributeBuilder;
import org.logoce.lmf.core.lang.builder.EnumBuilder;

public final class EnumFieldBuilder implements DefinitionFieldBuilder<Enum<?>>
{
	public static final ClassName ENUM_TYPE = ClassName.get(Enum.class);
	public static final ClassName ENUM_BUILDER_TYPE = ClassName.get(EnumBuilder.class);
	private static final ClassName ENUM_ATTRIBUTE_BUILDER_TYPE = ClassName.get(EnumAttributeBuilder.class);

	@Override
	public FieldSpec build(Enum<?> input)
	{
		final var name = input.name();
		final var primitiveClassName = ClassName.get("", name);
		final var typedEnum = TypeParameter.of(ENUM_TYPE, primitiveClassName);
		final var initializerBuilder = CodeBlock.builder()
											   .add("new $T<$T>()", ENUM_BUILDER_TYPE, primitiveClassName);

		BuilderInitializerUtil.appendAttributes(input, initializerBuilder);
		input.attributes()
			 .forEach(attribute -> initializerBuilder.add(".addAttribute(() -> $L)",
														 enumAttributeInitializer(attribute)));
		initializerBuilder.add(".build()");

		final var constantName = GenUtils.toConstantCase(name);

		return FieldSpec.builder(typedEnum.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	private static CodeBlock enumAttributeInitializer(final EnumAttribute attribute)
	{
		final var unit = attribute.unit();
		if (unit == null)
		{
			throw new IllegalStateException("EnumAttribute \"" + attribute.name() + "\" has no unit");
		}

		final var typeHolder = TypeResolutionUtil.resolveTypeHolder(unit);
		final var typeName = GenUtils.toConstantCase(unit.name());

		final CodeBlock unitBlock;
		if (typeHolder != null && unit.lmContainer() instanceof MetaModel typeModel)
		{
			final var modelDefinition = ClassName.get(TargetPathUtil.packageName(typeModel),
													 typeModel.name() + "ModelDefinition");
			unitBlock = CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeName);
		}
		else
		{
			unitBlock = CodeBlock.of("$N.$N", typeHolder, typeName);
		}

		final var initializer = CodeBlock.builder().add("new $T()", ENUM_ATTRIBUTE_BUILDER_TYPE);
		BuilderInitializerUtil.appendAttributes(attribute, initializer);
		initializer.add(".unit(() -> $L)", unitBlock);
		initializer.add(".build()");
		return initializer.build();
	}
}
