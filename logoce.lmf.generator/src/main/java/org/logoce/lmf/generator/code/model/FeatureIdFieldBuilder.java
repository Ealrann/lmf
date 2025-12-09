package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.ModelUtil;

public final class FeatureIdFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?>>
{
	private final Group<?> group;

	public FeatureIdFieldBuilder(final Group<?> group)
	{
		this.group = group;
	}

	@Override
	public FieldSpec build(final Feature<?, ?> input)
	{
		final var constantName = GenUtils.toConstantCase(input.name());
		final var containerGroup = (Group<?>) input.lmContainer();

		if (group != containerGroup)
		{
			return FieldSpec.builder(int.class, constantName, modifiers)
							.initializer(parentInitializer(input))
							.build();
		}

		final var model = (MetaModel) ModelUtil.root(containerGroup);
		final var key = model.domain() + ":" + model.name() + ":" + containerGroup.name() + ":" + input.name();
		final int id = key.hashCode();

		return FieldSpec.builder(int.class, constantName, modifiers)
						.initializer("$L", id)
						.build();
	}

	private static CodeBlock parentInitializer(final Feature<?, ?> feature)
	{
		final var containerGroup = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(containerGroup);
		final var groupClass = ClassName.get(TargetPathUtil.packageName(model), containerGroup.name());
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("$T.FeatureIDs.$N", groupClass, constantFeatureName);
	}
}
