package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.generator.util.TypeResolutionUtil;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.GenericBuilder;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.util.ModelUtils;

public final class GenericFieldBuilder implements DefinitionFieldBuilder<Generic<?>>
{
	private static final TypeParameter GENERIC_TYPE = TypeParameter.of(ClassName.get(Generic.class), 1);
	private static final ClassName GENERIC_BUILDER_TYPE = ClassName.get(GenericBuilder.class);
	private static final ClassName GENERIC_EXTENSION_BUILDER_TYPE = ClassName.get(GenericExtensionBuilder.class);
	private static final ClassName GENERIC_PARAMETER_BUILDER_TYPE = ClassName.get(GenericParameterBuilder.class);
	private static final ClassName BT_TYPE = ClassName.get(BoundType.class);

	@Override
	public FieldSpec build(final Generic<?> generic)
	{
		final var constantName = GenUtils.toConstantCase(generic.name());
		final var typing = resolveGenericTyping(generic);
		final var initializerBuilder = generateGenericsCodeblock(generic, typing.builderType());

		return FieldSpec.builder(typing.fieldType(), constantName, modifiers)
						.initializer(initializerBuilder)
						.build();
	}

	private static CodeBlock generateGenericsCodeblock(final Generic<?> generic, final TypeName builderType)
	{
		final var builder = CodeBlock.builder()
									 .add(builderType != null ? "new $T()" : "new $T<>()", builderType != null
																							   ? builderType
																							   : GENERIC_BUILDER_TYPE);

		BuilderInitializerUtil.appendAttributes(generic, builder);

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

	public static CodeBlock genericParameterBlock(final GenericParameter parameter)
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
					final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
															  model.name() + "ModelDefinition");
					final var groupConstantName = GenUtils.toConstantCase(group.name());
					return CodeBlock.of("$T.Generics.$N.ALL.get($L)", modelDefinition, groupConstantName, index);
				}
			}

			final var model = (MetaModel) ModelUtils.root(type);
			final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
													  model.name() + "ModelDefinition");
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

	private static GenericTyping resolveGenericTyping(final Generic<?> generic)
	{
		final var extension = generic.extension();
		if (extension == null || extension.type() == null)
		{
			return new GenericTyping(GENERIC_TYPE.parametrizedWildcard(), null);
		}

		final var resolvedBaseType = resolveGenericBaseType(generic);
		final var boundType = extension.boundType();
		final var wildcard = switch (boundType)
		{
			case Super -> WildcardTypeName.supertypeOf(resolvedBaseType);
			case Extends -> WildcardTypeName.subtypeOf(resolvedBaseType);
			case null -> WildcardTypeName.subtypeOf(resolvedBaseType);
		};

		final var fieldType = ParameterizedTypeName.get(ClassName.get(Generic.class), wildcard);
		final var builderType = ParameterizedTypeName.get(GENERIC_BUILDER_TYPE, resolvedBaseType);
		return new GenericTyping(fieldType, builderType);
	}

	private static TypeName resolveBuilderParameterType(final GenericParameter parameter)
	{
		final var type = parameter.type();
		final var nestedParameters = parameter.parameters();

		if (type instanceof Generic<?> genericType)
		{
			if (!nestedParameters.isEmpty())
			{
				throw new IllegalArgumentException("Generic type parameter cannot declare nested parameters: " +
												   genericType.name());
			}
			final var placeholder = WildcardTypeName.subtypeOf(TypeName.OBJECT);
			return parameter.wildcard()
				   ? wildcard(TypeName.OBJECT, parameter.wildcardBoundType())
				   : placeholder;
		}

		final var baseType = TypeResolutionUtil.resolveSimpleType(type);
		final var nestedTypes = nestedParameters.stream()
												.map(GenericFieldBuilder::resolveBuilderParameterType)
												.toList();

		final var resolvedType = nestedTypes.isEmpty()
								 ? baseType.parametrized().box()
								 : parameterize(baseType, nestedTypes).box();

		if (!parameter.wildcard())
		{
			return resolvedType;
		}
		return parameter.wildcardBoundType() == BoundType.Super
			   ? WildcardTypeName.supertypeOf(resolvedType)
			   : resolvedType;
	}

	private static TypeName resolveGenericBaseType(final Generic<?> generic)
	{
		final var extension = generic.extension();
		if (extension == null || extension.type() == null)
		{
			return TypeName.OBJECT;
		}

		final var baseType = TypeResolutionUtil.resolveSimpleType(extension.type());
		final var parameters = extension.parameters()
										 .stream()
										 .map(GenericFieldBuilder::resolveBuilderParameterType)
										 .toList();
		return parameters.isEmpty()
			   ? baseType.parametrized().box()
			   : parameterize(baseType, parameters).box();
	}

	private static TypeName parameterize(final TypeParameter baseType, final java.util.List<? extends TypeName> parameterTypes)
	{
		if (baseType instanceof TypeParameter.SimpleTypeParameter simple)
		{
			return ParameterizedTypeName.get(simple.raw(),
											 parameterTypes.stream().map(TypeName::box).toArray(TypeName[]::new));
		}
		if (baseType instanceof TypeParameter.CombinedTypeParameter combined)
		{
			return ParameterizedTypeName.get(combined.raw(),
											 parameterTypes.stream().map(TypeName::box).toArray(TypeName[]::new));
		}

		throw new IllegalArgumentException("Type cannot be parameterized: " + baseType.getClass().getSimpleName());
	}

	private static TypeName wildcard(final TypeName type, final BoundType boundType)
	{
		return switch (boundType)
		{
			case Super -> WildcardTypeName.supertypeOf(type);
			case Extends -> WildcardTypeName.subtypeOf(type);
			case null -> WildcardTypeName.subtypeOf(type);
		};
	}

	private record GenericTyping(TypeName fieldType, TypeName builderType) {}
}
