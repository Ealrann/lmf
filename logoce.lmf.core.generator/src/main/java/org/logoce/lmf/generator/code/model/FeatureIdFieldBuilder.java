package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.api.util.ModelUtil;

public final class FeatureIdFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?, ?, ?>>
{
	private final Group<?> group;

	public FeatureIdFieldBuilder(final Group<?> group)
	{
		this.group = group;
	}

	@Override
	public FieldSpec build(final Feature<?, ?, ?, ?> input)
	{
		final var constantName = GenUtils.toConstantCase(input.name());
		final var declaringGroup = resolveDeclaringGroup(input);

		if (group != declaringGroup)
		{
			return FieldSpec.builder(int.class, constantName, modifiers)
							.initializer(parentInitializer(input))
							.build();
		}

		final var model = (MetaModel) ModelUtil.root(declaringGroup);
		final var key = model.domain() + ":" + model.name() + ":" + declaringGroup.name() + ":" + input.name();
		final int id = key.hashCode();

		return FieldSpec.builder(int.class, constantName, modifiers)
						.initializer("$L", id)
						.build();
	}

	private static CodeBlock parentInitializer(final Feature<?, ?, ?, ?> feature)
	{
		final var declaringGroup = resolveDeclaringGroup(feature);
		final var model = (MetaModel) ModelUtil.root(declaringGroup);
		final var groupClass = ClassName.get(TargetPathUtil.packageName(model), declaringGroup.name());
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("$T.FeatureIDs.$N", groupClass, constantFeatureName);
	}

	private static Group<?> resolveDeclaringGroup(final Feature<?, ?, ?, ?> feature)
	{
		final var containerGroup = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(containerGroup);

		Group<?> declaringGroup = containerGroup;

		for (final var candidate : model.groups())
		{
			if (!candidate.features().contains(feature))
			{
				continue;
			}
			if (hasSuperGroupWithFeature(candidate, feature))
			{
				continue;
			}
			declaringGroup = candidate;
			break;
		}

		return declaringGroup;
	}

	private static boolean hasSuperGroupWithFeature(final Group<?> group, final Feature<?, ?, ?, ?> feature)
	{
		for (final var include : group.includes())
		{
			if (!(include.group() instanceof Group<?> superGroup))
			{
				continue;
			}
			if (superGroup.features().contains(feature))
			{
				return true;
			}
			if (hasSuperGroupWithFeature(superGroup, feature))
			{
				return true;
			}
		}
		return false;
	}
}
