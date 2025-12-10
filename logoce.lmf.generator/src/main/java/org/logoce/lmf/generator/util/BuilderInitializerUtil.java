package org.logoce.lmf.generator.util;

import com.squareup.javapoet.CodeBlock;
import java.util.List;
import java.util.function.Predicate;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Attribute;

public final class BuilderInitializerUtil
{
	private BuilderInitializerUtil()
	{
	}

	public static void appendAttributes(final IFeaturedObject source,
										final CodeBlock.Builder initializer)
	{
		appendAttributes(source, initializer, attribute -> true);
	}

	public static void appendAttributes(final IFeaturedObject source,
										final CodeBlock.Builder initializer,
										final Predicate<Attribute<?, ?, ?, ?>> attributeFilter)
	{
		source.lmGroup()
			  .features()
			  .stream()
			  .filter(Attribute.class::isInstance)
			  .map(Attribute.class::cast)
			  .filter(attributeFilter::test)
			  .forEach(attribute -> appendAttribute(source, initializer, attribute));
	}

	private static void appendAttribute(final IFeaturedObject source,
										final CodeBlock.Builder initializer,
										final Attribute<?, ?, ?, ?> attribute)
	{
		final Object value = source.get(attribute);

		if (shouldSkipAttribute(attribute, value)) return;

		if (attribute.many())
		{
			final var methodName = toManyMethodName(attribute.name());
			final var values = (List<?>) value;
			final var listLiteral = CodeBlock.builder().add("$T.of(", ConstantTypes.LIST);
			for (int i = 0; i < values.size(); i++)
			{
				if (i > 0) listLiteral.add(", ");
				listLiteral.add("$L", literalFor(values.get(i)));
			}
			listLiteral.add(")");
			initializer.add(".$L($L)", methodName, listLiteral.build());
			return;
		}

		initializer.add(".$L($L)", attribute.name(), literalFor(value));
	}

	private static boolean shouldSkipAttribute(final Attribute<?, ?, ?, ?> attribute, final Object value)
	{
		if (value == null) return true;
		if (attribute.many()) return value instanceof List<?> list && list.isEmpty();

		final var defaultValue = attribute.defaultValue();
		if (defaultValue != null && defaultValue.equals(value)) return true;
		if (value instanceof Boolean bool && !bool && defaultValue == null) return true;
		return false;
	}

	private static String toManyMethodName(final String attributeName)
	{
		return "add" + GenUtils.capitalizeFirstLetter(attributeName);
	}

	private static CodeBlock literalFor(final Object value)
	{
		if (value instanceof String string) return CodeBlock.of("$S", string);
		if (value instanceof Character character) return CodeBlock.of("$S", character);
		if (value instanceof Enum<?> enumeration) return CodeBlock.of("$T.$L", enumeration.getClass(), enumeration.name());
		return CodeBlock.of("$L", value);
	}
}
