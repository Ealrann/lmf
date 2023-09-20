package isotropy.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.util.TypeParameter;
import org.logoce.adapter.api.Adapter;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.ModelExtender;

import java.util.List;

@ModelExtender(scope = Group.class)
@Adapter
public final class GroupBuilderClassType extends AbstractGroupType implements IAdapter
{
	private GroupBuilderClassType(final Group<?> group)
	{
		super(bakeValues(group));
	}

	private static Values bakeValues(final Group<?> group)
	{
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var mainClass = interfaceType.mainClass;
		final var raw = mainClass.raw();
		final var finalParameters = mainClass.parameters();
		final var builderName = ClassName.get(raw.packageName() + ".builder", raw.simpleName() + "Builder");
		final var groupType = TypeParameter.of(builderName, finalParameters);
		final var builderInterface = ClassName.get(raw.packageName() + "." + raw.simpleName(), "Builder");
		final var superInterface = TypeParameter.of(builderInterface, finalParameters);

		return new Values(groupType,
						  interfaceType.packageName + ".builder",
						  group,
						  List.of(superInterface.parametrized()),
						  interfaceType.detailedParameters);
	}
}
