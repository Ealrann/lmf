package org.logoce.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;
import org.logoce.lmf.core.lang.Alias;
import org.logoce.lmf.core.lang.builder.AliasBuilder;

public final class AliasFieldBuilder implements DefinitionFieldBuilder<Alias>
{
	public static final ClassName ALIAS_TYPE = ClassName.get(Alias.class);
	public static final ClassName ALIAS_BUILDER_TYPE = ClassName.get(AliasBuilder.class);

	@Override
	public FieldSpec build(Alias input)
	{
		final var name = input.name();
		final var javaName = javify(name);
		final var initializer = CodeBlock.builder().add("new $T()", ALIAS_BUILDER_TYPE);

		BuilderInitializerUtil.appendAttributes(input, initializer);

		initializer.add(".build()");

		return FieldSpec.builder(ALIAS_TYPE, javaName, modifiers)
						.initializer(initializer.build())
						.build();
	}

	public static String javify(String input)
	{
		return input.chars().mapToObj(AliasFieldBuilder::mapChar).reduce(CharConversion::join).orElseThrow().result;
	}

	private static CharConversion mapChar(int c)
	{
		return switch (c)
		{
			case '+' -> new CharConversion("PLUS", true);
			case '-' -> new CharConversion("MINUS", true);
			case '*' -> new CharConversion("STAR", true);
			case '/' -> new CharConversion("DIVIDE", true);
			case '[' -> new CharConversion("LSB", true);
			case ']' -> new CharConversion("RSB", true);
			case '.' -> new CharConversion("DOT", true);

			default -> new CharConversion(Character.toString(Character.toUpperCase(c)), false);
		};
	}

	record CharConversion(String result, boolean converted)
	{
		CharConversion join(CharConversion next)
		{
			final var newString = converted || next.converted ? result + '_' + next.result : result + next.result;
			return new CharConversion(newString, next.converted);
		}
	}
}
