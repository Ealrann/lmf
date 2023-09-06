package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.IFeaturedObject;

import java.util.List;
import java.util.stream.Stream;

public record Types(Group<?> group,
					ClassName interfaceName,
					TypeName superInterface,
					List<TypeVariableName> finalParameters,
					List<TypeVariableName> detailedParameters)
{
	private static final boolean INCLUDE_SELF_TYPE = false;

	public Types builder()
	{
		final var builderName = ClassName.get("", "Builder");
		final var rawSuperBuilder = ClassName.get(IFeaturedObject.Builder.class);
		final var rawParametrizedClass = GenUtils.parameterize(interfaceName, finalParameters);
		final var superBuilder = GenUtils.parameterize(rawSuperBuilder, List.of(rawParametrizedClass));

		return new Types(group, builderName, superBuilder, finalParameters, detailedParameters);
	}

	public static Types from(final Reference<?> refInclude, final Group<?> group)
	{
		final var superInterface = TypeResolutionUtil.resolveInclude(refInclude, group);
		final var model = (Model) group.lmContainer();
		final var interfaceName = ClassName.get(model.domain(), group.name());

		final var genericParameters = group.generics().stream().map(GenericParameter::fromGeneric).toList();

		final var varNameSelf = TypeVariableName.get("Self");
		final var rawStream = genericParameters.stream().map(GenericParameter::raw);
		final var typedStream = genericParameters.stream().map(GenericParameter::defined);
		final var rawParameters = INCLUDE_SELF_TYPE
								  ? Stream.concat(rawStream, Stream.of(varNameSelf)).toList()
								  : rawStream.toList();
		final var typedParameters = INCLUDE_SELF_TYPE
									? Stream.concat(typedStream, Stream.of(varNameSelf)).toList()
									: typedStream.toList();

		return new Types(group, interfaceName, superInterface.parametrized(), rawParameters, typedParameters);
	}
}
