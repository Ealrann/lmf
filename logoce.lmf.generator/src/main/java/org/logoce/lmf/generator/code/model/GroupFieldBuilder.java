package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.GroupBuilderClassType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.util.CodeblockBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.lang.impl.GroupImpl;
import org.logoce.lmf.model.lang.impl.ReferenceImpl;
import org.logoce.lmf.model.util.ModelUtils;

public final class GroupFieldBuilder implements DefinitionFieldBuilder<Group<?>>
{
	public static final ClassName GROUP_IMPL_TYPE = ClassName.get(GroupImpl.class);
	public static final ClassName REFERENCE_IMPL_TYPE = ClassName.get(ReferenceImpl.class);

	@Override
	public FieldSpec build(Group<?> group)
	{
		final var name = group.name();
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var builderType = group.concrete() ? group.adapt(GroupBuilderClassType.class) : null;
		final var typedGroup = ConstantTypes.GROUP.nest(interfaceType.parametrizedWildcard());
		final var constantName = GenUtils.toConstantCase(name);
		final var initializerBuilder = CodeBlock.builder();

		final var referenceBlockBuilder = new CodeblockBuilder<>(", ", GroupFieldBuilder::generateReferencesCodeblock);
		group.includes().forEach(referenceBlockBuilder::feed);

		final var genericBlock = group.generics().isEmpty()
								 ? CodeBlock.of("$T.of()", ConstantTypes.LIST)
								 : CodeBlock.of("Generics.$N", constantName);

		final var builderSupplierType = ClassName.get(BuilderSupplier.class);

		initializerBuilder.add("new $T<>(", GROUP_IMPL_TYPE)
						  .add("$S, $L, ", name, group.concrete())
						  .add("$T.of(", ConstantTypes.LIST)
						  .add(referenceBlockBuilder.build())
						  .add("), Features.$N.ALL,", constantName)
						  .add(genericBlock);

		if (builderType != null) initializerBuilder.add(", new $T($T::new)", builderSupplierType, builderType.raw());
		else initializerBuilder.add(", null");

		initializerBuilder.add(")");

		return FieldSpec.builder(typedGroup.parametrized(), constantName, modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	public static CodeBlock generateReferencesCodeblock(final Reference<?> reference)
	{
		final var genericsBlockBuilder = new CodeblockBuilder<>(", ", GroupFieldBuilder::generateGenericsCodeblock);
		final var group = reference.group();
		final var groupConstantName = GenUtils.toConstantCase(group.name());
		final var targetModel = (MetaModel) ModelUtils.root(group);
		final var sourceModel = (MetaModel) ModelUtils.root(reference);
		reference.parameters().forEach(genericsBlockBuilder::feed);

		final var builder = CodeBlock.builder().add("new $T<>(", REFERENCE_IMPL_TYPE);

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
