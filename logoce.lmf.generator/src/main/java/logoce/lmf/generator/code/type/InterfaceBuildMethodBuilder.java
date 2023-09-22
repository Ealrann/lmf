package logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import logoce.lmf.generator.adapter.GroupBuilderClassType;
import logoce.lmf.generator.adapter.GroupBuilderInterfaceType;
import logoce.lmf.model.lang.Group;
import logoce.lmf.generator.code.util.CodeBuilder;

import javax.lang.model.element.Modifier;

public class InterfaceBuildMethodBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	@Override
	public MethodSpec build(final Group<?> group)
	{
		final var builderClassType = group.adapt(GroupBuilderClassType.class);
		final var builderType = group.adapt(GroupBuilderInterfaceType.class);
		final var code = CodeBlock.builder().add("return new $T", builderClassType.raw());
		if (!builderType.detailedParameters.isEmpty())
		{
			code.add("<>");
		}
		code.add("()");

		return MethodSpec.methodBuilder("builder")
						 .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						 .returns(builderType.parametrized())
						 .addStatement(code.build())
						 .addTypeVariables(builderType.detailedParameters)
						 .build();
	}
}
