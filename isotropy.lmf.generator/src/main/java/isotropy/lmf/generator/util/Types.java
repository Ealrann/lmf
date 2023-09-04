package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.util.ModelUtils;

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

	public static Types build(final Reference<?> refInclude, final Group<?> group)
	{
		final var superInterface = resolveInclude(refInclude, group);
		final var model = (Model) group.lmContainer();
		final var interfaceName = ClassName.get(model.domain(), group.name());

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

		return new Types(group, interfaceName, superInterface.parametrized(), rawParameters, typedParameters);
	}

	private static TypeParameter resolveInclude(final Reference<?> refInclude, final Group<?> group)
	{
		if (refInclude != null)
		{
			final var params = GenUtils.toParameters(refInclude.parameters());
			final var refIncludeGroup = refInclude.group();
			final var model = (Model) refIncludeGroup.lmContainer();
			final var className = ClassName.get(model.domain(), refIncludeGroup.name());
			return TypeParameter.of(className, params);
		}
		else if (group.name()
					  .equals("LMObject"))
		{
			final var res = ClassName.get(IFeaturedObject.class);
			return TypeParameter.of(res, new ClassName[0]);
		}
		else
		{
			final var res = ClassName.get(LMObject.class);
			return TypeParameter.of(res, new ClassName[0]);
		}
	}

	private record GenericParameter(TypeVariableName raw, TypeVariableName typed)
	{
		static GenericParameter fromGeneric(Generic<?> generic)
		{
			final var type = generic.type();
			final var typeVariableNameRaw = TypeVariableName.get(generic.name());
			final var typeVariableNameTyped = type != null
											  ? TypeVariableName.get(generic.name(), resolveType(type))
											  : typeVariableNameRaw;

			return new GenericParameter(typeVariableNameRaw, typeVariableNameTyped);
		}

		private static ClassName resolveType(final Type<?> type)
		{
			final var model = (Model) ModelUtils.root(type);
			return ClassName.get(model.domain(), type.name());
		}
	}
}
