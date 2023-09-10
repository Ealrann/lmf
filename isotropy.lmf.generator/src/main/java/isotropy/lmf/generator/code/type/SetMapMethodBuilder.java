package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class SetMapMethodBuilder implements CodeBuilder<GroupGenerationContext, MethodSpec>
{
	public static final ClassName SETTER_MAP_CLASS = ClassName.get(FeatureSetter.class);

	@Override
	public MethodSpec build(final GroupGenerationContext context)
	{
		final var type = TypeParameter.of(SETTER_MAP_CLASS, context.interfaceType().parametrizedWildcard());

		return MethodSpec.methodBuilder("setterMap")
						 .addModifiers(Modifier.PROTECTED)
						 .returns(type.parametrized())
						 .addStatement("return SET_MAP")
						 .addAnnotation(Override.class)
						 .build();
	}
}
