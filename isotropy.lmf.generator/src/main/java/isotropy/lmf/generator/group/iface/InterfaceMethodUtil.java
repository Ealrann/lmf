package isotropy.lmf.generator.group.iface;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.generator.group.feature.FeatureResolution;
import isotropy.lmf.generator.group.feature.MethodBuilder;
import isotropy.lmf.generator.group.feature.MethodUtil;

import javax.lang.model.element.Modifier;
import java.util.Optional;

public final class InterfaceMethodUtil
{
	public static final Modifier[] METHOD_MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};
	public static final Modifier[] BUILDER_METHOD_MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};

	public static MethodBuilder methodBuilder()
	{
		return new MethodBuilder(METHOD_MODIFIERS,
								 FeatureResolution::name,
								 InterfaceMethodUtil::interfaceReturnType);
	}

	public static MethodBuilder builderMethodBuilder(TypeName typedBuilder)
	{
		return new MethodBuilder(BUILDER_METHOD_MODIFIERS,
								 MethodUtil::builderMethodName,
								 f -> typedBuilder,
								 Optional.of(InterfaceMethodUtil::builderInterfaceParameterType),
								 Optional.empty());
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
