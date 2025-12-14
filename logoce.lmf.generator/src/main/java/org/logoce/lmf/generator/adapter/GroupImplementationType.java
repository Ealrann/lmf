package org.logoce.lmf.generator.adapter;

import com.squareup.javapoet.ClassName;
import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.core.lang.Group;

import java.util.List;

@ModelExtender(scope = Group.class)
@Adapter
public final class GroupImplementationType extends AbstractGroupType implements IAdapter
{
	private GroupImplementationType(final Group<?> group)
	{
		super(bakeValues(group));
	}

	private static Values bakeValues(final Group<?> group)
	{
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var mainClass = interfaceType.mainClass;
		final var raw = mainClass.raw();
		final var finalParameters = mainClass.parameters();
		final var implementationName = ClassName.get(raw.packageName() + ".impl", raw.simpleName() + "Impl");
		final var interfaceTypeParameter = interfaceType.mainClass;
		final var groupType = TypeParameter.of(implementationName, finalParameters);

		return new Values(groupType,
						  interfaceType.packageName + ".impl",
						  group,
						  List.of(interfaceTypeParameter.parametrized()),
						  interfaceType.detailedParameters);
	}
}
