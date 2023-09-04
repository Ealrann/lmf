package isotropy.lmf.generator.group.iface;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.feature.FeatureMethodBuilder;
import isotropy.lmf.generator.code.feature.MethodUtil;

import javax.lang.model.element.Modifier;
import java.util.Optional;

public final class InterfaceMethodUtil
{
	public static final Modifier[] METHOD_MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};
	public static final Modifier[] BUILDER_METHOD_MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};

	public static FeatureMethodBuilder methodBuilder()
	{
		return new FeatureMethodBuilder(METHOD_MODIFIERS, FeatureResolution::name, InterfaceMethodUtil::interfaceReturnType);
	}

	public static FeatureMethodBuilder builderMethodBuilder(TypeName typedBuilder)
	{
		return new FeatureMethodBuilder(BUILDER_METHOD_MODIFIERS,
										MethodUtil::builderMethodName,
								 f -> typedBuilder,
										Optional.of(InterfaceMethodUtil::builderInterfaceParameterType),
										Optional.empty(),
										false);
	}

	private static TypeName interfaceReturnType(FeatureResolution resolution)
	{
		return resolution.effectiveType()
						 .parametrized();
	}

	private static ParameterSpec builderInterfaceParameterType(FeatureResolution resolution)
	{
		final var singleType = resolution.singleType();

		return ParameterSpec.builder(singleType.parametrized(), resolution.name())
							.build();
	}
}
