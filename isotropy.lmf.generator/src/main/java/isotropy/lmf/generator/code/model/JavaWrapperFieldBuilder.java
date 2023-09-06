package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.JavaWrapper;
import isotropy.lmf.core.lang.impl.JavaWrapperImpl;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public final class JavaWrapperFieldBuilder implements CodeBuilder<JavaWrapper<?>, FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC};
	public static final ClassName JAVA_WRAPPER_TYPE = ClassName.get(JavaWrapper.class);
	public static final ClassName JAVA_WRAPPER_IMPL_TYPE = ClassName.get(JavaWrapperImpl.class);

	@Override
	public FieldSpec build(JavaWrapper<?> input)
	{
		final var domain = input.domain();
		final var name = input.name();
		final var type = ClassName.get(domain, name);
		final var genericCount = genericCount(domain+"."+name);
		final var typed = TypeParameter.of(type, genericCount);

		final var fullType = TypeParameter.of(JAVA_WRAPPER_TYPE, typed.parametrized());
		final var constantName = GenUtils.toConstantCase(name);

		return FieldSpec.builder(fullType.parametrized(), constantName, modifiers)
						.initializer("new $T<>($S, $S)", JAVA_WRAPPER_IMPL_TYPE, name, domain)
						.build();
	}

	private static int genericCount(String qualifiedName)
	{
		try
		{
			final var clazz = Class.forName(qualifiedName);
			final var typeParameters = clazz.getTypeParameters();
			return typeParameters.length;
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}
}
