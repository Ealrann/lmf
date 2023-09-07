package isotropy.lmf.generator.code.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.Alias;
import isotropy.lmf.core.lang.impl.AliasImpl;
import isotropy.lmf.generator.util.ConstantTypes;

import java.util.List;

public final class AliasFieldBuilder implements DefinitionFieldBuilder<Alias>
{
	public static final ClassName ALIAS_TYPE = ClassName.get(Alias.class);
	public static final ClassName ALIAS_IMPL_TYPE = ClassName.get(AliasImpl.class);

	@Override
	public FieldSpec build(Alias input)
	{
		final var name = input.name();
		final var javaName = javify(name);
		final var wordList = listify(input.words());

		return FieldSpec.builder(ALIAS_TYPE, javaName, modifiers)
						.initializer("new $T($S, $T.of( " + wordList + "))",
									 ALIAS_IMPL_TYPE,
									 name,
									 ConstantTypes.LIST_CLASS_NAME)
						.build();
	}

	private static String listify(final List<String> words)
	{
		final var stringBuilder = new StringBuilder();
		stringBuilder.append('"');
		for (int i = 0; i < words.size(); i++)
		{
			stringBuilder.append(words.get(i));
			if (i < words.size() - 1)
			{
				stringBuilder.append("\", \"");
			}
		}
		stringBuilder.append('"');
		return stringBuilder.toString();
	}

	private static String javify(String input)
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
