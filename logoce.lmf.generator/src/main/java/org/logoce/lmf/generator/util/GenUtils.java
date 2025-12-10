package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.lang.Primitive;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class GenUtils
{
	public static final ClassName WILDCARD = ClassName.get("", "?");

	public static TypeName parameterize(final ClassName className, final List<? extends TypeName> parameters)
	{
		if (parameters.isEmpty())
		{
			return className;
		}
		else
		{
			final TypeName[] array = parameters.toArray(new TypeName[0]);
			return ParameterizedTypeName.get(className, array);
		}
	}

	public static String capitalizeFirstLetter(String str)
	{
		if (str == null || str.isEmpty()) return str;
		else return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}

	public static String toConstantCase(String camelCaseString)
	{
		final var builder = new StringBuilder();
		boolean first = true;
		char prev = ' ';
		for (int i = 0; i < camelCaseString.length(); i++)
		{
			final var current = camelCaseString.charAt(i);
			if (first)
			{
				first = false;
			}
			else
			{
				final var transition = Character.isLowerCase(current) && Character.isUpperCase(prev) && i > 1;
				if (transition) builder.append('_');
				builder.append(Character.toUpperCase(prev));
			}
			prev = current;
		}
		builder.append(Character.toUpperCase(prev));
		return builder.toString();
	}

	public static ClassName[] wildcardArray(int count)
	{
		final var genericParams = new ClassName[count];
		Arrays.fill(genericParams, WILDCARD);
		return genericParams;
	}

	public static List<ClassName> wildcardList(int count)
	{
		return Stream.generate(() -> WILDCARD).limit(count).toList();
	}

	public static Class<?> resolvePrimitiveClass(final Primitive primitive)
	{
		final var primitiveType = switch (primitive)
		{
			case Boolean -> boolean.class;
			case Int -> int.class;
			case Long -> long.class;
			case Float -> float.class;
			case Double -> double.class;
			case String -> String.class;
		};
		return primitiveType;
	}

	public static int genericCount(String qualifiedName)
	{
		try
		{
			final var clazz = Class.forName(qualifiedName);
			return clazz.getTypeParameters().length;
		}
		catch (ClassNotFoundException e)
		{
			// For custom wrapper types that are not yet compiled or not on the
			// generator classpath, fall back to "no generics". This keeps code
			// generation working for non-generic wrappers while avoiding hard
			// failures during composite builds.
			return 0;
		}
	}
}
