package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.util.*;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;

import java.util.List;

public final class FeaturesFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?>>
{
	public static final ClassName ATTRIBUTE_TYPE = ClassName.get(Attribute.class);
	public static final ClassName ATTRIBUTE_BUILDER_TYPE = ClassName.get(AttributeBuilder.class);
	public static final ClassName RELATION_TYPE = ClassName.get(Relation.class);
	public static final ClassName RELATION_BUILDER_TYPE = ClassName.get(RelationBuilder.class);

	private final Group<?> group;

	public FeaturesFieldBuilder(Group<?> group)
	{
		this.group = group;
	}

	@Override
	public FieldSpec build(Feature<?, ?> input)
	{
		final var name = input.name();
		final var parentGroup = (Group<?>) input.lmContainer();
		final var resolvedFeature = input.adapt(FeatureResolution.class);
		final var specializeInherited = resolvedFeature.requiresOwnerSpecialization(group);
		final Group<?> targetGroup = specializeInherited ? group : parentGroup;
		final var constantName = GenUtils.toConstantCase(name);
		final var types = List.of(resolvedFeature.rawSingleTypeFor(targetGroup).parametrizedWildcard(),
								  resolvedFeature.rawEffectiveTypeFor(targetGroup).parametrizedWildcard());
		final var isAttribute = input instanceof Attribute<?, ?>;
		final var mainType = isAttribute
							 ? TypeParameter.of(ATTRIBUTE_TYPE, types)
							 : TypeParameter.of(RELATION_TYPE, types);

		final var initBuilder = CodeBlock.builder();

			if (!specializeInherited && group != parentGroup)
			{
				initBuilder.add(parentInitializer(input));
			}
			else
			{
				final var builderType = isAttribute
										? TypeParameter.of(ATTRIBUTE_BUILDER_TYPE, types)
										: TypeParameter.of(RELATION_BUILDER_TYPE, types);

				initBuilder.add("new $T()", builderType.parametrized());

				BuilderInitializerUtil.appendAttributes(input, initBuilder,
														attribute -> attribute.rawFeature() != Feature.Features.rawFeature);

				final var model = (MetaModel) ModelUtil.root(targetGroup);
				final var domainType = ClassName.get(TargetPathUtil.packageName(model), targetGroup.name());
				initBuilder.add(".rawFeature($T.Features.$N)", domainType, name);

				if (isAttribute)
				{
					final var attribute = (Attribute<?, ?>) input;
					final var datatype = attribute.datatype();
					if (datatype instanceof Generic<?> generic)
					{
						final var boundType = TypeResolutionUtil.resolveGenericBindingType(generic, targetGroup);
						if (boundType != null)
						{
							final var typeHolder = TypeResolutionUtil.resolveTypeHolder(boundType);
							final var typeName = GenUtils.toConstantCase(boundType.name());
							CodeBlock datatypeBlock;
							if (boundType.lmContainer() instanceof MetaModel typeModel)
							{
								final var parentModel = (MetaModel) targetGroup.lmContainer();
								if (typeModel != parentModel)
							{
								final var modelDefinition = ClassName.get(TargetPathUtil.packageName(typeModel),
																		  typeModel.name() + "ModelDefinition");
								datatypeBlock = CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeName);
							}
							else
							{
								datatypeBlock = CodeBlock.of("$N.$N", typeHolder, typeName);
							}
							}
							else
							{
								datatypeBlock = CodeBlock.of("$N.$N", typeHolder, typeName);
							}

							initBuilder.add(".datatype(() -> $L)", datatypeBlock);
						}
						else
						{
							final var rawType = resolvedFeature.rawSingleTypeFor(targetGroup).parametrized();
							final var genericRef = referenceGeneric(generic);
							initBuilder.add(".datatype(() -> ($T<$T>) $L)", ClassName.get(Datatype.class), rawType, genericRef);
						}
					}
					else
					{
						final var typeHolder = TypeResolutionUtil.resolveTypeHolder(datatype);
						final var typeName = GenUtils.toConstantCase(datatype.name());

					CodeBlock datatypeBlock;
					if (datatype != null && typeHolder != null && datatype.lmContainer() instanceof MetaModel typeModel)
					{
						final var parentModel = (MetaModel) parentGroup.lmContainer();
						if (typeModel != parentModel)
						{
							final var modelDefinition = ClassName.get(TargetPathUtil.packageName(typeModel),
																	  typeModel.name() + "ModelDefinition");
							datatypeBlock = CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeName);
						}
						else
						{
							datatypeBlock = CodeBlock.of("$N.$N", typeHolder, typeName);
						}
					}
					else
					{
						datatypeBlock = CodeBlock.of("$N.$N", typeHolder, typeName);
					}

					initBuilder.add(".datatype(() -> $L)", datatypeBlock);
				}

			}
			else
			{
				final var relation = (Relation<?, ?>) input;
				final var conceptBlock = generateConceptCodeblock(relation.concept());

				initBuilder.add(".concept($L)", conceptBlock);
			}

				input.parameters()
					 .forEach(parameter -> initBuilder.add(".addParameter(() -> $L)",
														  generateParameterCodeblock(parameter)));

			initBuilder.add(".build()");
		}

		return FieldSpec.builder(mainType.parametrized(), constantName, modifiers)
						.initializer(initBuilder.build())
						.build();
	}

	private static CodeBlock parentInitializer(final Feature<?, ?> feature)
	{
		final var group = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) group.lmContainer();
		final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
												  model.name() + "ModelDefinition");
		final var constantGroupName = GenUtils.toConstantCase(group.name());
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("$T.Features.$N.$N", modelDefinition, constantGroupName, constantFeatureName);
	}

	private static CodeBlock generateConceptCodeblock(final Concept<?> concept)
	{
		if (concept == null)
		{
			return CodeBlock.of("() -> null");
		}

		if (concept instanceof Generic<?> generic && generic.extension() != null && generic.extension().type() != null)
		{
			// When a relation concept is a Generic with an explicit bound (for example
			// Condition<T extends Parameter> and value : T), static feature metadata
			// cannot use the type variable T directly. Use the bound type instead so
			// that Relation<Bound, Bound> and its concept supplier remain valid in a
			// non-generic context.
			final var boundType = generic.extension().type();
			if (boundType instanceof Group<?> group)
			{
				final var model = (MetaModel) ModelUtil.root(group);
				final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
														  model.name() + "ModelDefinition");
				final var constantName = GenUtils.toConstantCase(group.name());
				return CodeBlock.builder().add("() -> $T.Groups.$N", modelDefinition, constantName).build();
			}
		}

		return generateGenericsCodeblock(concept);
	}

	private static CodeBlock generateParameterCodeblock(final org.logoce.lmf.model.lang.GenericParameter parameter)
	{
		return GenericFieldBuilder.genericParameterBlock(parameter);
	}

	private static CodeBlock referenceGeneric(final Generic<?> generic)
	{
		final var group = (Group<?>) generic.lmContainer();
		final var model = (MetaModel) ModelUtil.root(group);
		final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
												  model.name() + "ModelDefinition");
		final var constantName = GenUtils.toConstantCase(group.name());
		final var index = group.generics().indexOf(generic);
		return CodeBlock.builder()
						.add("$T.Generics.$N.ALL.get($L)", modelDefinition, constantName, index)
						.build();
	}

	private static CodeBlock generateGenericsCodeblock(final Concept<?> concept)
	{
		return switch (concept)
		{
			case Generic<?> generic ->
			{
				yield CodeBlock.builder()
							   .add("() -> $L", referenceGeneric(generic))
							   .build();
			}
			case Group<?> group ->
			{
				final var model = (MetaModel) ModelUtil.root(group);
				final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
														  model.name() + "ModelDefinition");
				final var constantName = GenUtils.toConstantCase(group.name());
				yield CodeBlock.builder().add("() -> $T.Groups.$N", modelDefinition, constantName).build();
			}
			case JavaWrapper<?> javaWrapper ->
			{
				final var model = (MetaModel) ModelUtil.root(javaWrapper);
				final var modelDefinition = ClassName.get(TargetPathUtil.packageName(model),
														  model.name() + "ModelDefinition");
				final var constantName = GenUtils.toConstantCase(javaWrapper.name());
				yield CodeBlock.builder().add("() -> $T.JavaWrappers.$N", modelDefinition, constantName).build();
			}
			default -> throw new IllegalArgumentException("Unsupported generic parameter: " + concept);
		};
	}
}
