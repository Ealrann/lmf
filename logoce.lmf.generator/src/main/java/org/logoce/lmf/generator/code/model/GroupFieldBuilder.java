package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import java.util.List;
import org.logoce.lmf.generator.adapter.GroupBuilderClassType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;
import org.logoce.lmf.generator.util.CodeblockBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.generator.util.TypeResolutionUtil;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.builder.GroupBuilder;
import org.logoce.lmf.model.lang.builder.IncludeBuilder;
import org.logoce.lmf.model.util.ModelUtils;

public final class GroupFieldBuilder implements DefinitionFieldBuilder<Group<?>>
{
	public static final ClassName GROUP_BUILDER_TYPE = ClassName.get(GroupBuilder.class);
	public static final ClassName INCLUDE_BUILDER_TYPE = ClassName.get(IncludeBuilder.class);

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

		initializerBuilder.add("new $T<$T>()", GROUP_BUILDER_TYPE, builderTargetType);

		BuilderInitializerUtil.appendAttributes(group, initializerBuilder);

		group.includes().forEach(include -> initializerBuilder.add(".addInclude(() -> $L)",
																   generateReferencesCodeblock(include)));

		initializerBuilder.add(".addFeatures(Features.$N.ALL)", constantName);

		if (!group.generics().isEmpty())
		{
			initializerBuilder.add(".addGenerics(Generics.$N.ALL)", constantName);
		}

		if (builderType != null) initializerBuilder.add(".lmBuilder(new $T<>($T::new))", builderSupplierRaw, builderType.raw());

		initializerBuilder.add(".build()");

		return FieldSpec.builder(typedGroup.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	public static CodeBlock generateReferencesCodeblock(final Include<?> reference)
	{
		final var parametersBlockBuilder = new CodeblockBuilder<>(", ", GenericFieldBuilder::genericParameterBlock);
		final var group = reference.group();
		final var groupConstantName = GenUtils.toConstantCase(group.name());
		final var targetModel = (MetaModel) ModelUtils.root(group);
		final var sourceModel = (MetaModel) ModelUtils.root(reference);
		final var includeType = TypeResolutionUtil.parametrizedType(group, List.of()).parametrizedWildcard();
		reference.parameters().forEach(parametersBlockBuilder::feed);

		final var builder = CodeBlock.builder()
									 .add("new $T()", ParameterizedTypeName.get(INCLUDE_BUILDER_TYPE, includeType));

		if (targetModel == sourceModel)
		{
			builder.add(".group(() -> $N)", groupConstantName);
		}
		else
		{
			final var modelDefinition = ClassName.get(TargetPathUtil.packageName(targetModel),
													  targetModel.name() + "Definition");
			builder.add(".group(() -> $T.Groups.$N)", modelDefinition, groupConstantName);
		}

		if (!reference.parameters().isEmpty())
		{
			reference.parameters()
					 .forEach(param -> builder.add(".addParameter(() -> $L)", GenericFieldBuilder.genericParameterBlock(param)));
		}

		return builder.add(".build()").build();
	}
}
