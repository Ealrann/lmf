package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GroupType;

import javax.lang.model.element.Modifier;

public class InterfaceBuildMethodBuilder implements CodeBuilder<GroupType, MethodSpec>
{
	@Override
	public MethodSpec build(final GroupType interfaceType)
	{
		final var builderType = interfaceType.builderInterface();
		final var builderClassType = interfaceType.builderClass();
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
