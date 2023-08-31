package isotropy.lmf.generator.model.feature;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.generator.util.GenUtils;

import javax.lang.model.element.Modifier;

public final class BuilderMethodBuilder
{
	public static final Modifier[] MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};

	private static final String PREFIX = "add";

	private final TypeName typedBuilder;

	public BuilderMethodBuilder(TypeName typedBuilder)
	{
		this.typedBuilder = typedBuilder;
	}

	public MethodSpec build(FeatureResolution rawResolution)
	{
		final var feature = rawResolution.feature();
		final var featureName = feature.name();
		final var singleType = rawResolution.singleType();
		final var name = feature.many() ? PREFIX + GenUtils.capitalizeFirstLetter(featureName) : featureName;

		return MethodSpec.methodBuilder(name)
						 .addModifiers(MODIFIERS)
						 .returns(typedBuilder)
						 .addParameter(singleType.parametrized(), featureName)
						 .build();
	}
}
