package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.MethodSpec;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.adapter.ModelResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.ModelUtil;

import javax.lang.model.element.Modifier;

public class LMGroupMethodBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	@Override
	public MethodSpec build(final Group<?> group)
	{
		final var model = (MetaModel) ModelUtil.root(group);
		final var modelDefinition = model.adapt(ModelResolution.class).modelDefinition;
		final var typedInterface = group.adapt(GroupInterfaceType.class);
		final var groupType = ConstantTypes.GROUP.nest(typedInterface.parametrizedWildcard());
		final var groupName = GenUtils.toConstantCase(group.name());

		return MethodSpec.methodBuilder("lmGroup")
						 .addModifiers(Modifier.PUBLIC)
						 .returns(groupType.parametrized())
						 .addStatement("return $T.Groups." + groupName, modelDefinition)
						 .addAnnotation(Override.class)
						 .build();
	}
}
