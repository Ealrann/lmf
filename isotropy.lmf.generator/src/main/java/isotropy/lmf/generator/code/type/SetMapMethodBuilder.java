package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.adapter.GroupResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class SetMapMethodBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	public static final ClassName SETTER_MAP_CLASS = ClassName.get(FeatureSetter.class);

	@Override
	public MethodSpec build(final Group<?> group)
	{
		final var type = TypeParameter.of(SETTER_MAP_CLASS,
										  group.adapt(GroupResolution.class)
											   .interfaceType
											   .parametrizedWildcard());

		return MethodSpec.methodBuilder("setterMap")
						 .addModifiers(Modifier.PROTECTED)
						 .returns(type.parametrized())
						 .addStatement("return SET_MAP")
						 .addAnnotation(Override.class)
						 .build();
	}
}
