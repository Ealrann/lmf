package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.builder.JavaWrapperBuilder;
import org.logoce.lmf.model.lang.builder.SerializerBuilder;

public final class JavaWrapperFieldBuilder implements DefinitionFieldBuilder<JavaWrapper<?>>
{
	public static final ClassName JAVA_WRAPPER_TYPE = ClassName.get(JavaWrapper.class);
	public static final ClassName JAVA_WRAPPER_BUILDER_TYPE = ClassName.get(JavaWrapperBuilder.class);
	public static final ClassName SERIALIZER_BUILDER_TYPE = ClassName.get(SerializerBuilder.class);

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
		final var serializer = input.serializer();
		final var serializerInit = serializer == null
								   ? CodeBlock.of("() -> null")
								   : CodeBlock.builder()
											   .add("() -> new $T()", SERIALIZER_BUILDER_TYPE)
											   .add(".create($S)", serializer.create())
											   .add(".convert($S)", serializer.convert())
											   .add(".build()")
											   .build();

		return FieldSpec.builder(fullType.parametrized(), constantName, modifiers)
						.initializer(CodeBlock.builder()
											  .add("new $T<$T>()", JAVA_WRAPPER_BUILDER_TYPE, typed.parametrized())
											  .add(".name($S)", name)
											  .add(".qualifiedClassName($S)", qualifiedName)
											  .add(".serializer($L)", serializerInit)
											  .add(".build()")
											  .build())
						.build();
	}
}
