package isotropy.lmf.generator.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.generator.util.GenUtils;

import java.util.List;
import java.util.stream.Stream;

public record Types(Group<?> group,
					ClassName className,
					TypeName superType,
					List<TypeVariableName> rawParameters,
					List<TypeVariableName> typedParameters)
{
	private static final boolean INCLUDE_SELF_TYPE = false;

	public Types builder()
	{
		final var builderName = ClassName.get("", "Builder");
		final var rawSuperBuilder = ClassName.get(IFeaturedObject.Builder.class);
		final var rawParametrizedClass = GenUtils.parameterize(className, rawParameters);
		final var superBuilder = GenUtils.parameterize(rawSuperBuilder, List.of(rawParametrizedClass));

		return new Types(group, builderName, superBuilder, rawParameters, typedParameters);
	}

	public static Types build(final Reference<?> refInclude, final Group<?> group)
	{
		final var superClass = resolveInclude(refInclude, group);
		final var model = (Model) group.lmContainer();
		final var className = ClassName.get(model.domain(), group.name());

		final var genericParameters = group.generics()
										   .stream()
										   .map(GenericParameter::fromGeneric)
										   .toList();

		final var varNameSelf = TypeVariableName.get("Self");
		final var rawStream = genericParameters.stream()
											   .map(GenericParameter::raw);
		final var typedStream = genericParameters.stream()
												 .map(GenericParameter::typed);
		final var rawParameters = INCLUDE_SELF_TYPE ? Stream.concat(rawStream, Stream.of(varNameSelf))
															.toList() : rawStream.toList();
		final var typedParameters = INCLUDE_SELF_TYPE ? Stream.concat(typedStream, Stream.of(varNameSelf))
															  .toList() : typedStream.toList();

		return new Types(group, className, superClass.parametrized(), rawParameters, typedParameters);
	}

	private static ParameteredType resolveInclude(final Reference<?> refInclude, final Group<?> group)
	{
		if (refInclude != null)
		{
			final var params = GenUtils.toParameters(refInclude.parameters());
			final var refIncludeGroup = refInclude.group();
			final var model = (Model) refIncludeGroup.lmContainer();
			final var className = ClassName.get(model.domain(), refIncludeGroup.name());
			return new ParameteredType(className, params);
		}
		else if (group.name()
					  .equals("LMObject"))
		{
			final var res = ClassName.get(IFeaturedObject.class);
			return new ParameteredType(res, new ClassName[0]);
		}
		else
		{
			final var res = ClassName.get(LMObject.class);
			return new ParameteredType(res, new ClassName[0]);
		}
	}

	private record ParameteredType(ClassName raw, ClassName[] params)
	{
		TypeName parametrized()
		{
			return params.length == 0 ? raw : ParameterizedTypeName.get(raw, params);
		}
	}

	private record GenericParameter(TypeVariableName raw, TypeVariableName typed)
	{
		static GenericParameter fromGeneric(Generic<?> generic)
		{
			final var type = generic.type();
			final var typeVariableNameRaw = TypeVariableName.get(generic.name());
			final var typeVariableNameTyped = type != null
											  ? TypeVariableName.get(generic.name(),
																	 ClassName.get("", type.name()))
											  : typeVariableNameRaw;

			return new GenericParameter(typeVariableNameRaw, typeVariableNameTyped);
		}
	}
}
