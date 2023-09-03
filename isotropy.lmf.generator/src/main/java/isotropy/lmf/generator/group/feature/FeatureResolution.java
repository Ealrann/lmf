package isotropy.lmf.generator.group.feature;

import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.generator.util.TypeParameter;

public record FeatureResolution(Feature<?, ?> feature, TypeParameter singleType, TypeParameter effectiveType)
{
	public static FeatureResolution from(Feature<?, ?> feature)
	{
		final var singleTypeParameter = MethodUtil.resolveType(feature);
		final var effectiveType = MethodUtil.effectiveType(feature, singleTypeParameter);

		return new FeatureResolution(feature, singleTypeParameter, effectiveType);
	}

	public String name()
	{
		return feature.name();
	}
}
