package isotropy.lmf.generator.code.feature;

import isotropy.lmf.generator.util.GenUtils;

public final class MethodUtil
{
	private static final String PREFIX = "add";

	public static String builderMethodName(final FeatureResolution f)
	{
		return f.feature().many() ? PREFIX + GenUtils.capitalizeFirstLetter(f.name()) : f.name();
	}
}
