package org.logoce.lmf.generator.code.util;

import com.squareup.javapoet.CodeBlock;
import org.logoce.lmf.generator.code.feature.FeatureParameter;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Relation;

import java.util.List;

public class ImplementationCodeUtil
{
	public static CodeBlock assignationStatement(final Feature<?, ?> feature, final String paramName)
	{
		if (feature.many())
		{
			if (feature.immutable())
			{
				return CodeBlock.of("this.$N = List.copyOf($N)", paramName, paramName);
			}
			return CodeBlock.of("this.$N.addAll($N)", paramName, paramName);
		}

		return CodeBlock.of("this.$N = $N", paramName, paramName);
	}

	public static List<CodeBlock> featureReturnStatement(FeatureParameter parameter)
	{
		final var name = parameter.parameterName();
		final var feature = parameter.feature().feature();
		if (feature instanceof Relation<?, ?> relation && relation.lazy())
		{
			if (feature.many())
			{
				if (!feature.immutable())
				{
					throw new IllegalStateException("Cannot generate getter for lazy, many and mutable feature '"
													+ feature.name() + '\'');
				}
				return List.of(CodeBlock.of("return $T.collectSuppliers($N)", ConstantTypes.BUILD_UTILS, name));
			}
			else
			{
				return List.of(CodeBlock.of("return $N.get()", name));
			}
		}
		else
		{
			return List.of(CodeBlock.of("return $N", name));
		}
	}
}
