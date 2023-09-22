package logoce.lmf.generator.code.type;

import com.squareup.javapoet.MethodSpec;
import logoce.lmf.generator.adapter.GroupInterfaceType;
import logoce.lmf.generator.util.TypeParameter;
import logoce.lmf.model.feature.FeatureGetter;
import logoce.lmf.model.lang.Group;
import logoce.lmf.generator.code.util.CodeBuilder;

import javax.lang.model.element.Modifier;

public class GetMapMethodBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	public static final TypeParameter GETTER_MAP_CLASS = TypeParameter.of(FeatureGetter.class);

	@Override
	public MethodSpec build(final Group<?> group)
	{
		final var type = GETTER_MAP_CLASS.nest(group.adapt(GroupInterfaceType.class).parametrizedWildcard());

		return MethodSpec.methodBuilder("getterMap")
						 .addModifiers(Modifier.PROTECTED)
						 .returns(type.parametrized())
						 .addStatement("return GET_MAP")
						 .addAnnotation(Override.class)
						 .build();
	}
}
