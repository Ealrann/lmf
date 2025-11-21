package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.*;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.GenericBuilder;
import org.logoce.lmf.model.util.ModelUtils;

public final class GenericFieldBuilder implements DefinitionFieldBuilder<Group<?>>
{
	private static final TypeParameter GENERIC_TYPE = TypeParameter.of(ClassName.get(Generic.class), 1);
	private static final TypeParameter LIST_OF_GENERIC = TypeParameter.of(ConstantTypes.LIST,
																		  GENERIC_TYPE.parametrizedWildcard());
	private static final ClassName GENERIC_BUILDER_TYPE = ClassName.get(GenericBuilder.class);
	private static final ClassName BT_TYPE = ClassName.get(BoundType.class);

	@Override
	public FieldSpec build(Group<?> input)
	{
		final var name = input.name();
		final var constantName = GenUtils.toConstantCase(name);
		final var initializerBuilder = CodeBlock.builder();
		final var genericBlockBuilder = new CodeblockBuilder<>(", ", GenericFieldBuilder::generateGenericsCodeblock);

		input.generics().forEach(genericBlockBuilder::feed);
		initializerBuilder.add("$T.of(", ConstantTypes.LIST).add(genericBlockBuilder.build()).add(")");

		return FieldSpec.builder(LIST_OF_GENERIC.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	private static CodeBlock generateGenericsCodeblock(final Generic<?> generic)
	{
		final var builder = CodeBlock.builder()
									 .add("new $T<>()", GENERIC_BUILDER_TYPE)
									 .add(".name($S)", generic.name())
									 .add(".type(() -> $L)", typeBlock(generic.type()));

		final var boundType = generic.boundType();
		if (boundType != null)
		{
			builder.add(".boundType($T.$L)", BT_TYPE, boundType);
		}

		return builder.add(".build()").build();
	}

	private static CodeBlock typeBlock(Type<?> type)
	{
		if (type != null)
		{
			final var model = (MetaModel) ModelUtils.root(type);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var typeConstantName = GenUtils.toConstantCase(type.name());
			final var typeHolder = TypeResolutionUtil.resolveTypeHolder(type);
			return CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeConstantName);
		}
		else
		{
			return CodeBlock.of("null");
		}
	}
}
