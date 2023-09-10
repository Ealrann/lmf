package org.logoce.extender.impl.util;

import org.logoce.extender.api.IAdaptable;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.parameter.IParameterResolver;
import org.logoce.extender.api.reflect.ConstructorHandle;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;

public final class ExtenderBuilder<Extender extends IAdapter>
{
	private final ConstructorHandle<Extender> constructorHandle;
	private final Class<Extender> extenderClass;

	public ExtenderBuilder(final ConstructorHandle<Extender> constructorHandle, final Class<Extender> extenderClass)
	{
		this.constructorHandle = constructorHandle;
		this.extenderClass = extenderClass;
	}

	public Extender build(final IAdaptable target,
						  final Stream<? extends IParameterResolver> resolvers) throws ReflectiveOperationException
	{
		final var resolverList = resolvers.toList();
		final var javaConstructor = constructorHandle.getJavaConstructor();
		final var paramTypes = javaConstructor.getParameterTypes();
		final var paramAnnotations = javaConstructor.getParameterAnnotations();
		final int size = paramTypes.length;
		final Object[] parameters = new Object[size];

		for (int i = 0; i < size; i++)
		{
			final var paramType = paramTypes[i];
			final Annotation[] annotations = paramAnnotations[i];
			final var annotation = annotations.length > 0 ? annotations[0] : null;

			if (paramType.isInstance(target))
			{
				parameters[i] = target;
			}
			else
			{
				try
				{
					final var resolver = findResolver(resolverList, paramType, annotation);
					parameters[i] = resolver.resolve(target, paramType);
				}
				catch (Exception e)
				{
					final var message = "Cannot resolve parameter " + i + " of constructor " + extenderClass.getSimpleName();
					throw new ReflectiveOperationException(message, e);
				}
			}
		}

		return constructorHandle.newInstance(parameters);
	}

	private static IParameterResolver findResolver(final List<? extends IParameterResolver> resolvers,
												   final Class<?> paramType,
												   final Annotation annotation) throws ReflectiveOperationException
	{
		return resolvers.stream()
						.filter(resolver -> resolver.isApplicable(paramType, annotation))
						.findAny()
						.orElseThrow(ReflectiveOperationException::new);
	}
}
