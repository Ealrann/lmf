package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;

import java.util.List;

public final class FeatureIdFieldBuilder implements DefinitionFieldBuilder<Feature<?, ?>>
{
	private final List<Feature<?, ?>> features;

	public FeatureIdFieldBuilder(final Group<?> group)
	{
		this.features = FeatureStreams.distinctFeatures(group).toList();
	}

	@Override
	public FieldSpec build(final Feature<?, ?> input)
	{
		final var constantName = GenUtils.toConstantCase(input.name());
		final var index = features.indexOf(input);

		return FieldSpec.builder(int.class, constantName, modifiers)
						.initializer("$L", index)
						.build();
	}
}

