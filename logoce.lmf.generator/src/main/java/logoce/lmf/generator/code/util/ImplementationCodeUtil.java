package logoce.lmf.generator.code.util;

import com.squareup.javapoet.CodeBlock;
import logoce.lmf.generator.util.ConstantTypes;
import logoce.lmf.model.lang.Feature;
import logoce.lmf.model.lang.Relation;
import logoce.lmf.generator.code.feature.FeatureParameter;

import java.util.List;

public class ImplementationCodeUtil
{
	public static CodeBlock assignationStatement(final Feature<?, ?> feature, final String paramName)
	{
		final var assignPattern = feature.many() ? "this.$N = List.copyOf($N)" : "this.$N = $N";
		return CodeBlock.of(assignPattern, paramName, paramName);
	}

	public static CodeBlock notificationStatement(final String name)
	{
		final var statement = "lNotify(RelationNotificationBuilder.insert(this, Features.$N, $N))";
		return CodeBlock.of(statement, name, name);
	}

	public static CodeBlock containmentSetStatement(final String name)
	{
		final var statement = "setContainer($N, Features.$N)";
		return CodeBlock.of(statement, name, name);
	}

	public static List<CodeBlock> featureReturnStatement(FeatureParameter parameter)
	{
		final var name = parameter.parameterName();
		final var feature = parameter.feature().feature();
		if (feature instanceof Relation<?, ?> relation && relation.lazy())
		{
			if (feature.many())
			{
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
