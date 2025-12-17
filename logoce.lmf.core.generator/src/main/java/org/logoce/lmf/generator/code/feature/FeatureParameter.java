package org.logoce.lmf.generator.code.feature;

import com.squareup.javapoet.ParameterSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;

public record FeatureParameter(FeatureResolution feature, ParameterSpec parameterSpec)
{
	public String parameterName()
	{
		if (parameterSpec != null)
		{
			return parameterSpec.name;
		}
		else
		{
			return feature.name();
		}
	}
}
