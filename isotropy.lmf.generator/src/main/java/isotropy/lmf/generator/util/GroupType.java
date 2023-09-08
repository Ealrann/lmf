package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Reference;

import java.util.List;
import java.util.stream.Stream;

public final class GroupType implements TypeParameter
{
	private static final boolean INCLUDE_SELF_TYPE = false;

	private final TypeParameter mainClass;
	public final TypeName superInterface;
	public final Group<?> group;
	public final List<TypeVariableName> detailedParameters;

	public GroupType(final Group<?> group,
					 final TypeParameter mainClass,
					 final TypeName superInterface,
					 final List<TypeVariableName> detailedParameters)
	{

		this.group = group;
		this.mainClass = mainClass;
		this.superInterface = superInterface;
		this.detailedParameters = detailedParameters;
	}

	@Override
	public ClassName raw()
	{
		return mainClass.raw();
	}

	@Override
	public TypeName parametrized()
	{
		return mainClass.parametrized();
	}

	@Override
	public TypeName parametrizedWildcard()
	{
		return mainClass.parametrizedWildcard();
	}

	@Override
	public List<? extends TypeName> parameters()
	{
		return mainClass.parameters();
	}

	public TypeSpec.Builder interfaceSpecBuilder()
	{
		return TypeSpec.interfaceBuilder(mainClass.raw())
					   .addSuperinterface(superInterface)
					   .addTypeVariables(detailedParameters);
	}

	public TypeSpec.Builder classSpecBuilder()
	{
		return TypeSpec.classBuilder(mainClass.raw())
					   .addSuperinterface(superInterface)
					   .addTypeVariables(detailedParameters);
	}

	public GroupType builderInterface()
	{
		final var raw = mainClass.raw();
		final var finalParameters = mainClass.parameters();
		final var builderName = ClassName.get(raw.packageName() + "." + raw.simpleName(), "Builder");
		final var rawParametrizedClass = GenUtils.parameterize(raw, finalParameters);
		final var superBuilder = GenUtils.parameterize(ConstantTypes.FEATURED_OBJECT_BUILDER,
													   List.of(rawParametrizedClass));
		final var builderType = TypeParameter.of(builderName, finalParameters);
		return new GroupType(group, builderType, superBuilder, detailedParameters);
	}

	public GroupType builderClass()
	{
		final var raw = mainClass.raw();
		final var finalParameters = mainClass.parameters();
		final var builderInterface = ClassName.get(raw.packageName() + "." + raw.simpleName(), "Builder");
		final var builderName = ClassName.get(raw.packageName() + ".builder", raw.simpleName() + "Builder");
		final var builderType = TypeParameter.of(builderName, finalParameters);
		final var superInterface = TypeParameter.of(builderInterface, finalParameters);
		return new GroupType(group, builderType, superInterface.parametrized(), detailedParameters);
	}

	public GroupType implementation()
	{
		final var raw = mainClass.raw();
		final var finalParameters = mainClass.parameters();
		final var implementationName = ClassName.get(raw.packageName() + ".impl", raw.simpleName() + "Impl");
		final var implType = TypeParameter.of(implementationName, finalParameters);
		return new GroupType(group, implType, mainClass.parametrized(), detailedParameters);
	}

	public static GroupType from(final Reference<?> refInclude, final Group<?> group)
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
		final var groupType = TypeParameter.of(interfaceName, rawParameters);
		return new GroupType(group, groupType, superInterface.parametrized(), typedParameters);
	}
}
