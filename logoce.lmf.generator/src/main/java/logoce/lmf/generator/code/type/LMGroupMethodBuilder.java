package logoce.lmf.generator.code.type;

import com.squareup.javapoet.MethodSpec;
import logoce.lmf.generator.adapter.GroupInterfaceType;
import logoce.lmf.generator.adapter.ModelResolution;
import logoce.lmf.generator.util.ConstantTypes;
import logoce.lmf.generator.util.GenUtils;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.util.ModelUtils;
import logoce.lmf.generator.code.util.CodeBuilder;

import javax.lang.model.element.Modifier;

public class LMGroupMethodBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	@Override
	public MethodSpec build(final Group<?> group)
	{
		final var model = (Model) ModelUtils.root(group);
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
