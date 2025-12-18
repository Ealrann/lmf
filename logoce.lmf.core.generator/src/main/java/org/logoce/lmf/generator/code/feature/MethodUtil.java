package org.logoce.lmf.generator.code.feature;

import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.util.GenUtils;

import javax.lang.model.SourceVersion;

public final class MethodUtil
{
	private static final String PREFIX = "add";

	public static String builderMethodName(final FeatureResolution f)
	{
		final var name = f.name();
		return f.feature.many() ? PREFIX + GenUtils.capitalizeFirstLetter(singularize(name)) : name;
	}

	public static String builderSingleParameterName(final FeatureResolution f)
	{
		final var featureName = f.name();
		final var many = f.feature.many();
		final var resolved = many ? singularize(featureName) : featureName;

		return validateParameterName(resolved);
	}

	public static String validateParameterName(final String name)
	{
		final var isValidResolution = SourceVersion.isName(name);

		return isValidResolution ? name : '_' + name;
	}

	private static String singularize(final String name)
	{
		if (name == null || name.isEmpty())
		{
			return name;
		}
		if (name.equalsIgnoreCase("aliases"))
		{
			return name.substring(0, name.length() - 2);
		}
		return removeLastS(name);
	}

	private static String removeLastS(String name)
	{
		final var length = name.length();
		final var finishesWithS = name.charAt(length - 1) == 's';
		return !finishesWithS ? name : name.substring(0, length - 1);
	}
}
