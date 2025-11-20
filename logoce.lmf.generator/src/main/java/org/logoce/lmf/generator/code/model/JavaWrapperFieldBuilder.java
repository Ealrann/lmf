package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.impl.JavaWrapperImpl;

public final class JavaWrapperFieldBuilder implements DefinitionFieldBuilder<JavaWrapper<?>>
{
	public static final ClassName JAVA_WRAPPER_TYPE = ClassName.get(JavaWrapper.class);
	public static final ClassName JAVA_WRAPPER_IMPL_TYPE = ClassName.get(JavaWrapperImpl.class);

	@Override
	public FieldSpec build(JavaWrapper<?> input)
	{
		final var name = input.name();
		final var qualifiedName = input.qualifiedClassName();
		final var type = ClassName.bestGuess(qualifiedName);
		final var genericCount = GenUtils.genericCount(qualifiedName);
		final var typed = TypeParameter.of(type, genericCount);

		final var fullType = TypeParameter.of(JAVA_WRAPPER_TYPE, typed.parametrized());
		final var constantName = GenUtils.toConstantCase(name);

		return FieldSpec.builder(fullType.parametrized(), constantName, modifiers)
						.initializer("new $T<>($S, $S)", JAVA_WRAPPER_IMPL_TYPE, name, qualifiedName)
						.build();
	}
}
