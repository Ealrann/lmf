package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.adapter.GroupInterfaceType;
import isotropy.lmf.generator.adapter.ModelResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.ConstantTypes;
import isotropy.lmf.generator.util.GenUtils;

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
