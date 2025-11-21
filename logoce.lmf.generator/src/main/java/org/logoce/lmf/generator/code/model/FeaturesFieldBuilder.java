package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.util.*;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.lang.impl.ReferenceImpl;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.List;

public final class FeaturesFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?>>
{
	public static final ClassName ATTRIBUTE_TYPE = ClassName.get(Attribute.class);
	public static final ClassName ATTRIBUTE_BUILDER_TYPE = ClassName.get(AttributeBuilder.class);
	public static final ClassName RELATION_TYPE = ClassName.get(Relation.class);
	public static final ClassName RELATION_BUILDER_TYPE = ClassName.get(RelationBuilder.class);
	public static final ClassName REFERENCE_IMPL_TYPE = ClassName.get(ReferenceImpl.class);

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
		final var constantName = GenUtils.toConstantCase(name);
		final var resolvedFeature = input.adapt(FeatureResolution.class);
		final var types = List.of(resolvedFeature.singleType().parametrizedWildcard(),
								  resolvedFeature.effectiveType().parametrizedWildcard());
		final var isAttribute = input instanceof Attribute<?, ?>;
		final var mainType = isAttribute
							 ? TypeParameter.of(ATTRIBUTE_TYPE, types)
							 : TypeParameter.of(RELATION_TYPE, types);

		final var initBuilder = CodeBlock.builder();

		if (group != parentGroup)
		{
			initBuilder.add(parentInitializer(input));
		}
		else
		{
			final var builderType = isAttribute
									? TypeParameter.of(ATTRIBUTE_BUILDER_TYPE, types)
									: TypeParameter.of(RELATION_BUILDER_TYPE, types);

			initBuilder.add("new $T()", builderType.parametrized())
					   .add(".name($S)", name)
					   .add(".immutable($L)", input.immutable())
					   .add(".many($L)", input.many())
					   .add(".mandatory($L)", input.mandatory())
					   .add(".rawFeature($N.Features.$N)", parentGroup.name(), name);

			if (isAttribute)
			{
				final var attribute = (Attribute<?, ?>) input;
				final var datatype = attribute.datatype();
				final var typeHolder = TypeResolutionUtil.resolveTypeHolder(datatype);
				final var typeName = GenUtils.toConstantCase(datatype.name());

				CodeBlock datatypeBlock;
				if (datatype != null && typeHolder != null && datatype.lmContainer() instanceof MetaModel typeModel)
				{
					final var parentModel = (MetaModel) parentGroup.lmContainer();
					if (typeModel != parentModel)
					{
						final var modelDefinition = ClassName.get(typeModel.domain(), typeModel.name() + "Definition");
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

				initBuilder.add(".datatype(() -> $L)", datatypeBlock)
						   .add(".defaultValue($S)", attribute.defaultValue());
			}
			else
			{
				final var relation = (Relation<?, ?>) input;
				final var reference = relation.reference();
				final var refBlock = generateReferencesCodeblock(reference);

				initBuilder.add(".reference(() -> $L)", refBlock)
						   .add(".lazy($L)", relation.lazy())
						   .add(".contains($L)", relation.contains());
			}

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
		final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
		final var constantGroupName = GenUtils.toConstantCase(group.name());
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("$T.Features.$N.$N", modelDefinition, constantGroupName, constantFeatureName);
	}

	public static CodeBlock generateReferencesCodeblock(final Reference<?> reference)
	{
		final var genericsBlockBuilder = new CodeblockBuilder<>(", ", FeaturesFieldBuilder::generateGenericsCodeblock);
		final var group = reference.group();
		final var groupConstantName = GenUtils.toConstantCase(group.name());
		reference.parameters().forEach(genericsBlockBuilder::feed);

		final var targetModel = (MetaModel) ModelUtils.root(group);
		final var sourceModel = (MetaModel) ModelUtils.root(reference);
		final var builder = CodeBlock.builder().add("new $T<>(", REFERENCE_IMPL_TYPE);

		if (targetModel == sourceModel)
		{
			final var conceptHolder = TypeResolutionUtil.resolveConceptHolder(group);
			builder.add("() -> $N.$N, ", conceptHolder, groupConstantName);
		}
		else
		{
			final var modelDefinition = ClassName.get(targetModel.domain(), targetModel.name() + "Definition");
			builder.add("() -> $T.Groups.$N, ", modelDefinition, groupConstantName);
		}

		return builder.add("$T.of(", ConstantTypes.LIST)
					  .add(genericsBlockBuilder.build())
					  .add("))")
					  .build();
	}

	private static CodeBlock generateGenericsCodeblock(final LMEntity<?> lmEntity)
	{
		return switch (lmEntity)
		{
			case Generic<?> generic ->
			{
				final var group = (Group<?>) generic.lmContainer();
				final var model = (MetaModel) ModelUtils.root(group);
				final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
				final var constantName = GenUtils.toConstantCase(group.name());
				final var index = group.generics().indexOf(generic);
				yield CodeBlock.builder()
							   .add("() -> $T.Generics.$N.get($L)", modelDefinition, constantName, index)
							   .build();
			}
			case Group<?> group ->
			{
				final var model = (MetaModel) ModelUtils.root(group);
				final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
				final var constantName = GenUtils.toConstantCase(group.name());
				yield CodeBlock.builder().add("() -> $T.Groups.$N", modelDefinition, constantName).build();
			}
			case JavaWrapper<?> javaWrapper ->
			{
				final var model = (MetaModel) ModelUtils.root(javaWrapper);
				final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
				final var constantName = GenUtils.toConstantCase(javaWrapper.name());
				yield CodeBlock.builder().add("() -> $T.JavaWrappers.$N", modelDefinition, constantName).build();
			}
			default -> throw new IllegalArgumentException("Unsupported generic parameter: " + lmEntity);
		};
	}
}
