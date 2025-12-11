package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.util.*;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;

import java.util.ArrayList;
import java.util.List;

public final class FeaturesFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?, ?, ?>>
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
	public FieldSpec build(Feature<?, ?, ?, ?> input)
	{
		final var name = input.name();
		final var resolvedFeature = input.adapt(FeatureResolution.class);
		final var declaringGroup = resolveDeclaringGroup(input);
		final var specializeInherited = resolvedFeature.requiresOwnerSpecialization(group);
		final Group<?> targetGroup = specializeInherited ? group : declaringGroup;
		final var constantName = GenUtils.toConstantCase(name);
		final var rawSingleType = resolvedFeature.rawSingleTypeFor(targetGroup).parametrizedWildcard();
		final var rawEffectiveType = resolvedFeature.rawEffectiveTypeFor(targetGroup).parametrizedWildcard();
		final var listenerType = resolveListenerType(input, rawEffectiveType);
		final List<TypeName> baseBuilderTypes = List.of(rawSingleType, rawEffectiveType);

		final var ownerType = resolveOwnerFeaturesType(declaringGroup);
		final var builderTypes = builderTypeArguments(input, baseBuilderTypes, listenerType, ownerType);
		final var featureTypes = featureTypeArguments(input, builderTypes);
		final var isAttribute = input instanceof Attribute<?, ?, ?, ?>;
		final var mainType = isAttribute
							 ? TypeParameter.of(ATTRIBUTE_TYPE, featureTypes)
							 : TypeParameter.of(RELATION_TYPE, featureTypes);

		final var initBuilder = CodeBlock.builder();

		if (!specializeInherited && group != declaringGroup)
		{
			initBuilder.add("$L", parentInitializer(input));
		}
		else
		{
			final var builderType = isAttribute
									? TypeParameter.of(ATTRIBUTE_BUILDER_TYPE, builderTypes)
									: TypeParameter.of(RELATION_BUILDER_TYPE, builderTypes);

			initBuilder.add("new $T()", builderType.parametrized());

			BuilderInitializerUtil.appendAttributes(input,
													initBuilder,
													attribute -> !attribute.name().equals("id"));

			final var model = (MetaModel) ModelUtil.root(declaringGroup);
			final var declaringGroupType = ClassName.get(TargetPathUtil.packageName(model), declaringGroup.name());
			initBuilder.add(".id($T.FeatureIDs.$N)", declaringGroupType, constantName);

			if (isAttribute)
			{
				final var attribute = (Attribute<?, ?, ?, ?>) input;
				final var datatype = attribute.datatype();
				if (datatype instanceof Generic<?> generic)
				{
					final var boundType = TypeResolutionUtil.resolveGenericBindingType(generic, targetGroup);
						if (boundType != null)
						{
							final var typeHolder = TypeResolutionUtil.resolveTypeHolder(boundType);
							final var typeName = GenUtils.toConstantCase(boundType.name());
							final CodeBlock datatypeBlock;
							if (boundType.lmContainer() instanceof MetaModel typeModel)
							{
								final var modelDefinition = ClassName.get(TargetPathUtil.packageName(typeModel),
																		  typeModel.name() + "ModelDefinition");
								datatypeBlock = CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeName);
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

						final CodeBlock datatypeBlock;
						if (typeHolder != null && datatype.lmContainer() instanceof MetaModel typeModel)
						{
							final var modelDefinition = ClassName.get(TargetPathUtil.packageName(typeModel),
																	  typeModel.name() + "ModelDefinition");
							datatypeBlock = CodeBlock.of("$T.$N.$N", modelDefinition, typeHolder, typeName);
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
				final var relation = (Relation<?, ?, ?, ?>) input;
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

	private static List<TypeName> builderTypeArguments(final Feature<?, ?, ?, ?> feature,
													   final List<TypeName> baseTypes,
													   final TypeName listenerType,
													   final TypeName ownerType)
	{
		final var featureGenericArity = 4;
		if (featureGenericArity <= baseTypes.size())
		{
			return baseTypes;
		}

		if (!(feature instanceof Attribute<?, ?, ?, ?>) && !(feature instanceof Relation<?, ?, ?, ?>))
		{
			return baseTypes;
		}

		final var result = new ArrayList<TypeName>(featureGenericArity);
		result.addAll(baseTypes);
		result.add(listenerType);
		result.add(ownerType);
		return result;
	}

	private static List<TypeName> featureTypeArguments(final Feature<?, ?, ?, ?> feature,
													   final List<TypeName> baseTypes)
	{
		final var featureGenericArity = 4;
		if (featureGenericArity <= baseTypes.size())
		{
			return baseTypes;
		}

		if (!(feature instanceof Attribute<?, ?, ?, ?>) && !(feature instanceof Relation<?, ?, ?, ?>))
		{
			return baseTypes;
		}

		final var result = new ArrayList<TypeName>(featureGenericArity);
		result.addAll(baseTypes);

		for (int i = baseTypes.size(); i < featureGenericArity; i++)
		{
			result.add(GenUtils.WILDCARD);
		}

		return result;
	}

	private static CodeBlock parentInitializer(final Feature<?, ?, ?, ?> feature)
	{
		final var declaringGroup = resolveDeclaringGroup(feature);
		final var model = (MetaModel) ModelUtil.root(declaringGroup);
		final var groupClass = ClassName.get(TargetPathUtil.packageName(model), declaringGroup.name());
		final var groupFeaturesInterface = groupClass.nestedClass("Features");
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("$T.$N", groupFeaturesInterface, constantFeatureName);
	}

	private static TypeName resolveListenerType(final Feature<?, ?, ?, ?> feature,
												final TypeName rawEffectiveType)
	{
		if (feature instanceof Attribute<?, ?, ?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			if (datatype instanceof Unit<?> unit && !attribute.many())
			{
				return switch (unit.primitive())
				{
					case Boolean -> ClassName.get("org.logoce.lmf.model.notification.listener", "BooleanListener");
					case Int -> ClassName.get("org.logoce.lmf.model.notification.listener", "IntListener");
					case Long -> ClassName.get("org.logoce.lmf.model.notification.listener", "LongListener");
					case Float -> ClassName.get("org.logoce.lmf.model.notification.listener", "FloatListener");
					case Double -> ClassName.get("org.logoce.lmf.model.notification.listener", "DoubleListener");
					case String ->
							GenUtils.parameterize(ClassName.get("org.logoce.lmf.model.notification.listener", "Listener"),
												  List.of(ClassName.get(String.class)));
				};
			}

			return GenUtils.parameterize(ClassName.get("org.logoce.lmf.model.notification.listener", "Listener"),
										 List.of(rawEffectiveType.box()));
		}

		if (feature instanceof Relation<?, ?, ?, ?> relation && !relation.many())
		{
			return GenUtils.parameterize(ClassName.get("org.logoce.lmf.model.notification.listener", "Listener"),
										 List.of(rawEffectiveType.box()));
		}

		return GenUtils.parameterize(ClassName.get("org.logoce.lmf.model.notification.listener", "Listener"),
									 List.of(rawEffectiveType.box()));
	}

	private static Group<?> resolveDeclaringGroup(final Feature<?, ?, ?, ?> feature)
	{
		final var containerGroup = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(containerGroup);
		final List<Group<?>> owners = new ArrayList<>();

		for (final var group : model.groups())
		{
			if (group.features().contains(feature))
			{
				owners.add(group);
			}
		}

		if (owners.isEmpty())
		{
			return containerGroup;
		}

		for (final var candidate : owners)
		{
			boolean hasParent = false;
			for (final var other : owners)
			{
				if (other != candidate && ModelUtil.isSubGroup(other, candidate))
				{
					hasParent = true;
					break;
				}
			}

			if (!hasParent)
			{
				return candidate;
			}
		}

		return containerGroup;
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

	private static TypeName resolveOwnerFeaturesType(final Group<?> declaringGroup)
	{
		final var model = (MetaModel) ModelUtil.root(declaringGroup);
		final var ownerType = ClassName.get(TargetPathUtil.packageName(model), declaringGroup.name());
		final var featuresType = ownerType.nestedClass("Features");
		return GenUtils.parameterize(featuresType, List.of(GenUtils.WILDCARD));
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
