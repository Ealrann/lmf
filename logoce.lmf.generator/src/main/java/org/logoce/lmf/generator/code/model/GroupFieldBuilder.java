package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.GroupBuilderClassType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.util.CodeblockBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeResolutionUtil;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.GroupBuilder;
import org.logoce.lmf.model.lang.impl.IncludeImpl;
import org.logoce.lmf.model.util.ModelUtils;

public final class GroupFieldBuilder implements DefinitionFieldBuilder<Group<?>>
{
	public static final ClassName GROUP_BUILDER_TYPE = ClassName.get(GroupBuilder.class);
	public static final ClassName INCLUDE_IMPL_TYPE = ClassName.get(IncludeImpl.class);

	@Override
	public FieldSpec build(Group<?> group)
	{
		final var name = group.name();
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var builderType = group.concrete() ? group.adapt(GroupBuilderClassType.class) : null;
		final var builtType = interfaceType.parametrizedWildcard();
		final var typedGroup = ConstantTypes.GROUP.nest(builtType);
		final var builderTargetType = builtType;
		final var constantName = GenUtils.toConstantCase(name);
		final var initializerBuilder = CodeBlock.builder();

		final var builderSupplierRaw = ClassName.get(BuilderSupplier.class);

		initializerBuilder.add("new $T<$T>()", GROUP_BUILDER_TYPE, builderTargetType)
						  .add(".name($S)", name)
						  .add(".concrete($L)", group.concrete());

		group.includes().forEach(include -> initializerBuilder.add(".addInclude(() -> $L)",
																   generateReferencesCodeblock(include)));

		final var features = ModelUtils.streamAllFeatures(group).toList();
		for (int i = 0; i < features.size(); i++)
		{
			initializerBuilder.add(".addFeature(() -> Features.$N.ALL.get($L))", constantName, i);
		}

		final var generics = group.generics();
		for (int i = 0; i < generics.size(); i++)
		{
			initializerBuilder.add(".addGeneric(() -> Generics.$N.get($L))", constantName, i);
		}

		if (builderType != null) initializerBuilder.add(".lmBuilder(new $T<>($T::new))", builderSupplierRaw, builderType.raw());

		initializerBuilder.add(".build()");

		return FieldSpec.builder(typedGroup.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	public static CodeBlock generateReferencesCodeblock(final Include<?> reference)
	{
		final var genericsBlockBuilder = new CodeblockBuilder<>(", ", GroupFieldBuilder::generateGenericsCodeblock);
		final var group = reference.group();
		final var groupConstantName = GenUtils.toConstantCase(group.name());
		final var targetModel = (MetaModel) ModelUtils.root(group);
		final var sourceModel = (MetaModel) ModelUtils.root(reference);
		reference.parameters().forEach(genericsBlockBuilder::feed);

		final var builder = CodeBlock.builder().add("new $T<>(", INCLUDE_IMPL_TYPE);

		if (targetModel == sourceModel)
		{
			builder.add("() -> $N, ", groupConstantName);
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
