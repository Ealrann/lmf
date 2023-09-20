package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.adapter.GroupInterfaceType;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class SetMapMethodBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	public static final TypeParameter SETTER_MAP_CLASS = TypeParameter.of(FeatureSetter.class);

	@Override
	public MethodSpec build(final Group<?> group)
	{
		final var type = SETTER_MAP_CLASS.nest(group.adapt(GroupInterfaceType.class).parametrizedWildcard());

		return MethodSpec.methodBuilder("setterMap")
						 .addModifiers(Modifier.PROTECTED)
						 .returns(type.parametrized())
						 .addStatement("return SET_MAP")
						 .addAnnotation(Override.class)
						 .build();
	}
}
