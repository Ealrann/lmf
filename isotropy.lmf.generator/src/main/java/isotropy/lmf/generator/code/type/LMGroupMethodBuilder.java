package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class LMGroupMethodBuilder implements CodeBuilder<TypeFeatures, MethodSpec>
{
	public static final ClassName GROUP_CLASS = ClassName.get(Group.class);

	@Override
	public MethodSpec build(final TypeFeatures context)
	{
		final var group = context.group();
		final var model = (Model) ModelUtils.root(group);
		final var modelDefinition = model.name() + "Definition";
		final var definitionClassName = ClassName.get(model.domain(), modelDefinition);
		final var typedInterface = context.interfaceType();
		final var groupType = TypeParameter.of(GROUP_CLASS, typedInterface.parametrizedWildcard());

		return MethodSpec.methodBuilder("lmGroup")
						 .addModifiers(Modifier.PUBLIC)
						 .returns(groupType.parametrized())
						 .addStatement("return $T.GROUPS." + group.name(), definitionClassName)
						 .addAnnotation(Override.class)
						 .build();
	}
}
