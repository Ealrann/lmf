package logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import logoce.lmf.generator.util.GenericParameter;
import logoce.lmf.generator.util.TypeParameter;
import logoce.lmf.generator.util.TypeResolutionUtil;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.lang.Reference;
import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;

import java.util.List;
import java.util.stream.Stream;

@ModelExtender(scope = Group.class)
@Adapter
public final class GroupInterfaceType extends AbstractGroupType implements IAdapter
{
	public static final TypeVariableName VAR_NAME_SELF = TypeVariableName.get("Self");
	private static final boolean INCLUDE_SELF_TYPE = false;

	private GroupInterfaceType(final Group<?> group)
	{
		super(bakeValues(group));
	}

	private static Values bakeValues(final Group<?> group)
	{
		final var model = (Model) group.lmContainer();
		final var interfaceName = ClassName.get(model.domain(), group.name());
		final var genericParameters = group.generics().stream().map(GenericParameter::fromGeneric).toList();
		final var rawStream = genericParameters.stream().map(GenericParameter::raw);
		final var includes = group.includes();
		final var superInterfaces = resolveIncludes(includes, group);
		final var typedStream = genericParameters.stream().map(GenericParameter::defined);
		final var rawParameters = INCLUDE_SELF_TYPE
								  ? Stream.concat(rawStream, Stream.of(VAR_NAME_SELF)).toList()
								  : rawStream.toList();
		final var typedParameters = INCLUDE_SELF_TYPE
									? Stream.concat(typedStream, Stream.of(VAR_NAME_SELF)).toList()
									: typedStream.toList();

		final var groupType = TypeParameter.of(interfaceName, rawParameters);

		return new Values(groupType, model.domain(), group, superInterfaces, typedParameters);
	}

	private static List<TypeName> resolveIncludes(final List<Reference<?>> includes, final Group<?> group)
	{
		if (includes.isEmpty())
		{
			return List.of(TypeResolutionUtil.resolveNoInclude(group).parametrized());
		}
		else
		{
			return includes.stream()
						   .map(i -> TypeResolutionUtil.resolveInclude(i, group))
						   .map(TypeParameter::parametrized)
						   .toList();
		}
	}
}
