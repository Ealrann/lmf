package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.util.*;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.impl.AttributeImpl;
import org.logoce.lmf.model.lang.impl.ReferenceImpl;
import org.logoce.lmf.model.lang.impl.RelationImpl;
import org.logoce.lmf.model.util.ModelUtils;

import java.util.List;

public final class FeaturesFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?>>
{
	public static final ClassName ATTRIBUTE_TYPE = ClassName.get(Attribute.class);
	public static final ClassName ATTRIBUTE_IMPL_TYPE = ClassName.get(AttributeImpl.class);
	public static final ClassName RELATION_TYPE = ClassName.get(Relation.class);
	public static final ClassName RELATION_IMPL_TYPE = ClassName.get(RelationImpl.class);
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
			initBuilder.add("new $T<>(", isAttribute ? ATTRIBUTE_IMPL_TYPE : RELATION_IMPL_TYPE)
					   .add("$S, ", name)
					   .add("$L, ", input.immutable())
					   .add("$L, ", input.many())
					   .add("$L, ", input.mandatory())
					   .add("$N.Features.$N, ", parentGroup.name(), name);

			if (isAttribute)
			{
				final var attribute = (Attribute<?, ?>) input;
				final var datatype = attribute.datatype();
				final var typeHolder = TypeResolutionUtil.resolveTypeHolder(datatype);
				final var typeName = GenUtils.toConstantCase(datatype.name());

				if (datatype != null && typeHolder != null && datatype.lmContainer() instanceof MetaModel typeModel)
				{
					final var parentModel = (MetaModel) parentGroup.lmContainer();
					if (typeModel != parentModel)
					{
						final var modelDefinition = ClassName.get(typeModel.domain(), typeModel.name() + "Definition");
						initBuilder.add("$T.$N.$N, ", modelDefinition, typeHolder, typeName);
					}
					else
					{
						initBuilder.add("$N.$N, ", typeHolder, typeName);
					}
				}
				else
				{
					initBuilder.add("$N.$N, ", typeHolder, typeName);
				}

				initBuilder.add("$S, ", attribute.defaultValue())
						   .add("$T.of()", ConstantTypes.LIST);
			}
			else
			{
				final var relation = (Relation<?, ?>) input;
				final var reference = relation.reference();
				final var refBlock = generateReferencesCodeblock(reference);

				initBuilder.add(refBlock).add(", $L, ", relation.lazy()).add("$L", relation.contains());
			}

			initBuilder.add(")");
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
		final var conceptHolder = TypeResolutionUtil.resolveConceptHolder(group);
		reference.parameters().forEach(genericsBlockBuilder::feed);

		return CodeBlock.builder()
						.add("new $T<>(() -> $N.$N, ", REFERENCE_IMPL_TYPE, conceptHolder, groupConstantName)
						.add("$T.of(", ConstantTypes.LIST)
						.add(genericsBlockBuilder.build())
						.add("))")
						.build();
	}

	private static CodeBlock generateGenericsCodeblock(final Concept<?> concept)
	{
		if (concept instanceof Generic<?> generic)
		{
			final var group = (Group<?>) generic.lmContainer();
			final var model = (MetaModel) ModelUtils.root(group);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var constantName = GenUtils.toConstantCase(group.name());
			final var index = group.generics().indexOf(generic);

			return CodeBlock.builder()
							.add("() -> $T.Generics.$N.get($L)", modelDefinition, constantName, index)
							.build();
		}
		else
		{
			final var group = (Group<?>) concept;
			final var model = (MetaModel) ModelUtils.root(group);
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			final var constantName = GenUtils.toConstantCase(group.name());

			return CodeBlock.builder().add("() -> $T.Groups.$N", modelDefinition, constantName).build();
		}
	}
}
