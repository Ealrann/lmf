package isotropy.lmf.generator.code.feature;

import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.generator.util.TypeParameter;
import isotropy.lmf.generator.util.TypeResolutionUtil;

public record FeatureResolution(Feature<?, ?> feature, TypeParameter singleType, TypeParameter effectiveType)
{
	public static FeatureResolution from(Feature<?, ?> feature)
	{
		final var singleTypeParameter = TypeResolutionUtil.resolveType(feature);
		final var effectiveType = TypeResolutionUtil.effectiveType(feature, singleTypeParameter);

		return new FeatureResolution(feature, singleTypeParameter, effectiveType);
	}

	public String name()
	{
		return feature.name();
	}
}
