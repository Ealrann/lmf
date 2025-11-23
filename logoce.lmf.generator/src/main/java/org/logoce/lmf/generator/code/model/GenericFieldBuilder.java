package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.CodeblockBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.generator.util.TypeResolutionUtil;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.GenericBuilder;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.util.ModelUtils;

public final class GenericFieldBuilder implements DefinitionFieldBuilder<Group<?>>
{
	private static final TypeParameter GENERIC_TYPE = TypeParameter.of(ClassName.get(Generic.class), 1);
	private static final TypeParameter LIST_OF_GENERIC = TypeParameter.of(ConstantTypes.LIST,
																		  GENERIC_TYPE.parametrizedWildcard());
	private static final ClassName GENERIC_BUILDER_TYPE = ClassName.get(GenericBuilder.class);
	private static final ClassName GENERIC_EXTENSION_BUILDER_TYPE = ClassName.get(GenericExtensionBuilder.class);
	private static final ClassName GENERIC_PARAMETER_BUILDER_TYPE = ClassName.get(GenericParameterBuilder.class);
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
									 .add(".name($S)", generic.name());

		final var extensionBlock = resolveExtensionBlock(generic);
		if (extensionBlock != null)
		{
			builder.add(".extension(() -> $L)", extensionBlock);
		}

		return builder.add(".build()").build();
	}

	private static CodeBlock resolveExtensionBlock(final Generic<?> generic)
	{
		final var extension = generic.extension();
		return extension != null ? genericExtensionBlock(extension) : null;
	}

	private static CodeBlock genericExtensionBlock(final GenericExtension extension)
	{
		final var builder = CodeBlock.builder()
									 .add("new $T()", GENERIC_EXTENSION_BUILDER_TYPE);

		final var type = extension.type();
		if (type != null)
		{
			builder.add(".type(() -> $L)", typeBlock(type));
		}

		final var parameters = extension.parameters();
		if (parameters != null && !parameters.isEmpty())
		{
			parameters.forEach(param -> builder.add(".addParameter(() -> $L)", genericParameterBlock(param)));
		}

		final var boundType = extension.boundType();
		if (boundType != null)
		{
			builder.add(".boundType($T.$L)", BT_TYPE, boundType);
		}

		return builder.add(".build()").build();
	}

	private static CodeBlock genericParameterBlock(final GenericParameter parameter)
	{
		final var builder = CodeBlock.builder()
									 .add("new $T()", GENERIC_PARAMETER_BUILDER_TYPE);

		if (parameter.wildcard())
		{
			builder.add(".wildcard(true)");
		}

		final var wildcardBoundType = parameter.wildcardBoundType();
		if (wildcardBoundType != null)
		{
			builder.add(".wildcardBoundType($T.$L)", BT_TYPE, wildcardBoundType);
		}

		final var type = parameter.type();
		if (type != null)
		{
			builder.add(".type(() -> $L)", typeBlock(type));
		}

		final var nestedParameters = parameter.parameters();
		if (nestedParameters != null && !nestedParameters.isEmpty())
		{
			nestedParameters.forEach(param -> builder.add(".addParameter(() -> $L)", genericParameterBlock(param)));
		}

		return builder.add(".build()").build();
	}

	private static CodeBlock typeBlock(Type<?> type)
	{
		if (type != null)
		{
			if (type instanceof Generic<?> generic)
			{
				final var container = generic.lmContainer();
				if (container instanceof Group<?> group)
				{
					final var index = group.generics().indexOf(generic);
					if (index < 0)
					{
						throw new IllegalStateException("Generic " + generic.name() + " not found in container");
					}
					final var model = (MetaModel) ModelUtils.root(group);
					final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
					final var groupConstantName = GenUtils.toConstantCase(group.name());
					return CodeBlock.of("$T.Generics.$N.get($L)", modelDefinition, groupConstantName, index);
				}
			}

			final var model = (MetaModel) ModelUtils.root(type);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var typeConstantName = GenUtils.toConstantCase(type.name());
			final var typeHolder = TypeResolutionUtil.resolveTypeHolder(type);
			if (typeHolder == null)
			{
				throw new IllegalArgumentException("Cannot resolve type holder for: " + type.getClass().getSimpleName());
			}
			return CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeConstantName);
		}
		else
		{
			return CodeBlock.of("null");
		}
	}
}
