package org.logoce.extender.api.reflect;

import org.logoce.extender.api.IAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public final class ReflectUtils
{
	private static final Class<?>[] NO_TYPES = new Class<?>[0];
	private static final Object[] NO_OBJECTS = new Object[0];

	public static <T extends IAdapter> T constructNew(Class<T> classifier)
	{
		try
		{
			final Constructor<T> constructor = classifier.getConstructor(NO_TYPES);
			return constructor.newInstance(NO_OBJECTS);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static <T extends Annotation> Optional<AnnotatedMethod<T>> gatherAnnotatedMethod(Class<?> type,
																							Class<T> annotationClass)
	{
		return streamAnnotatedMethods(type, annotationClass).findAny();
	}

	public static <T extends Annotation> Stream<AnnotatedMethod<T>> streamAnnotatedMethods(Class<?> type,
																						   Class<T> annotationClass)
	{
		return streamClassHierarchy(type).map(Class::getDeclaredMethods)
										 .flatMap(Arrays::stream)
										 .map(method -> buildAnnotatedMethod(method, annotationClass))
										 .filter(Optional::isPresent)
										 .map(Optional::get);
	}

	private static <T extends Annotation> Optional<AnnotatedMethod<T>> buildAnnotatedMethod(Method method,
																							Class<T> annotationClass)
	{
		final var annotation = method.getAnnotation(annotationClass);
		if (annotation != null)
		{
			return Optional.of(new AnnotatedMethod<>(method, annotation));
		}
		else
		{
			return Optional.empty();
		}
	}

	public static Stream<Class<?>> streamClassHierarchy(Class<?> type)
	{
		return Stream.iterate(type, o -> o != Object.class, Class::getSuperclass);
	}

	public static Stream<AnnotatedMethod<? extends Annotation>> streamAnnotatedMethods(Class<?> type)
	{
		return streamClassHierarchy(type).map(Class::getDeclaredMethods)
										 .flatMap(Arrays::stream)
										 .flatMap(ReflectUtils::streamAnnotatedMethod);
	}

	public static Stream<AnnotatedMethod<? extends Annotation>> streamAnnotatedMethod(Method method)
	{
		return Arrays.stream(method.getAnnotations())
					 .filter(ReflectUtils::isNotJavaAnnotation)
					 .map(annotation -> new AnnotatedMethod<>(method, annotation));
	}

	private static boolean isNotJavaAnnotation(final Annotation annotation)
	{
		final String packageName = annotation.annotationType().getPackageName();
		return !packageName.startsWith("java") && !packageName.startsWith("sun");
	}

	public record AnnotatedMethod<T extends Annotation>(Method method, T annotation)
	{
	}
}
