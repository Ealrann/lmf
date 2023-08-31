package isotropy.lmf.generator.model.feature;

import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;

public final class MethodBuilder
{
	private static final Modifier[] MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};

	public MethodSpec build(FeatureResolution effectiveResolution)
	{
		final var feature = effectiveResolution.feature();
		final var effectiveType = effectiveResolution.effectiveType();
		final var featureName = feature.name();

		return MethodSpec.methodBuilder(featureName)
						 .addModifiers(MODIFIERS)
						 .returns(effectiveType.parametrized())
						 .build();
	}
}
