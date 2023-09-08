package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class GetMapMethodBuilder implements CodeBuilder<GroupGenerationContext, MethodSpec>
{
	public static final ClassName GETTER_MAP_CLASS = ClassName.get(FeatureGetter.class);

	@Override
	public MethodSpec build(final GroupGenerationContext context)
	{
		final var type = TypeParameter.of(GETTER_MAP_CLASS, context.interfaceType().parametrizedWildcard());

		return MethodSpec.methodBuilder("getterMap")
						 .addModifiers(Modifier.PROTECTED)
						 .returns(type.parametrized())
						 .addStatement("return GET_MAP")
						 .addAnnotation(Override.class)
						 .build();
	}
}
