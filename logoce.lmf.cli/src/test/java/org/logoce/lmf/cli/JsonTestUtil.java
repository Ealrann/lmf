package org.logoce.lmf.cli;

import static org.junit.jupiter.api.Assertions.fail;

final class JsonTestUtil
{
	private JsonTestUtil()
	{
	}

	static void assertValidJson(final String jsonText)
	{
		if (jsonText == null)
		{
			fail("JSON was null");
			return;
		}

		try
		{
			final var scanner = new Scanner(jsonText);
			scanner.skipWhitespace();
			scanner.readValue();
			scanner.skipWhitespace();
			if (!scanner.eof())
			{
				fail("Trailing characters after JSON at index " + scanner.index);
			}
		}
		catch (RuntimeException e)
		{
			final var msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			fail("Invalid JSON: " + msg + "\nJSON:\n" + jsonText);
		}
	}

	private static final class Scanner
	{
		private final String text;
		private int index;

		private Scanner(final String text)
		{
			this.text = text;
		}

		boolean eof()
		{
			return index >= text.length();
		}

		void skipWhitespace()
		{
			while (index < text.length() && Character.isWhitespace(text.charAt(index)))
			{
				index++;
			}
		}

		char peek()
		{
			if (eof())
			{
				throw new IllegalStateException("Unexpected end of JSON input");
			}
			return text.charAt(index);
		}

		boolean peek(final char c)
		{
			return !eof() && text.charAt(index) == c;
		}

		char next()
		{
			if (eof())
			{
				throw new IllegalStateException("Unexpected end of JSON input");
			}
			return text.charAt(index++);
		}

		void expect(final char c)
		{
			final char got = next();
			if (got != c)
			{
				throw new IllegalStateException("Expected '" + c + "' but got '" + got + "' at index " + (index - 1));
			}
		}

		void readValue()
		{
			skipWhitespace();
			if (eof())
			{
				throw new IllegalStateException("Unexpected end of JSON input");
			}

			final char c = peek();
			if (c == '{')
			{
				readObject();
				return;
			}
			if (c == '[')
			{
				readArray();
				return;
			}
			if (c == '"')
			{
				readString();
				return;
			}
			if (c == '-' || (c >= '0' && c <= '9'))
			{
				readNumber();
				return;
			}

			if (tryConsumeLiteral("true") || tryConsumeLiteral("false") || tryConsumeLiteral("null"))
			{
				return;
			}

			throw new IllegalStateException("Unexpected token '" + c + "' at index " + index);
		}

		private boolean tryConsumeLiteral(final String literal)
		{
			if (text.regionMatches(index, literal, 0, literal.length()))
			{
				index += literal.length();
				return true;
			}
			return false;
		}

		private void readObject()
		{
			expect('{');
			skipWhitespace();
			if (peek('}'))
			{
				expect('}');
				return;
			}

			while (true)
			{
				skipWhitespace();
				readString();
				skipWhitespace();
				expect(':');
				readValue();
				skipWhitespace();
				if (peek(','))
				{
					expect(',');
					continue;
				}
				if (peek('}'))
				{
					expect('}');
					return;
				}
				throw new IllegalStateException("Expected ',' or '}' at index " + index);
			}
		}

		private void readArray()
		{
			expect('[');
			skipWhitespace();
			if (peek(']'))
			{
				expect(']');
				return;
			}

			while (true)
			{
				readValue();
				skipWhitespace();
				if (peek(','))
				{
					expect(',');
					continue;
				}
				if (peek(']'))
				{
					expect(']');
					return;
				}
				throw new IllegalStateException("Expected ',' or ']' at index " + index);
			}
		}

		private void readString()
		{
			expect('"');
			while (true)
			{
				if (eof())
				{
					throw new IllegalStateException("Unterminated JSON string");
				}

				final char c = next();
				if (c == '"')
				{
					return;
				}
				if (c == '\\')
				{
					readEscape();
					continue;
				}
				if (c < 0x20)
				{
					throw new IllegalStateException("Unescaped control character in string at index " + (index - 1));
				}
			}
		}

		private void readEscape()
		{
			if (eof())
			{
				throw new IllegalStateException("Unterminated escape sequence");
			}
			final char esc = next();
			switch (esc)
			{
				case '"', '\\', '/', 'b', 'f', 'n', 'r', 't' -> { return; }
				case 'u' ->
				{
					for (int i = 0; i < 4; i++)
					{
						if (eof())
						{
							throw new IllegalStateException("Invalid unicode escape");
						}
						final char c = next();
						final boolean hex = (c >= '0' && c <= '9')
										|| (c >= 'a' && c <= 'f')
										|| (c >= 'A' && c <= 'F');
						if (!hex)
						{
							throw new IllegalStateException("Invalid unicode escape");
						}
					}
					return;
				}
				default -> throw new IllegalStateException("Unsupported escape: \\" + esc);
			}
		}

		private void readNumber()
		{
			if (peek('-'))
			{
				expect('-');
			}

			if (peek('0'))
			{
				expect('0');
			}
			else
			{
				readDigits();
			}

			if (peek('.'))
			{
				expect('.');
				readDigits();
			}

			if (peek('e') || peek('E'))
			{
				next();
				if (peek('+') || peek('-'))
				{
					next();
				}
				readDigits();
			}
		}

		private void readDigits()
		{
			int start = index;
			while (!eof())
			{
				final char c = peek();
				if (c >= '0' && c <= '9')
				{
					index++;
					continue;
				}
				break;
			}
			if (index == start)
			{
				throw new IllegalStateException("Expected digits at index " + index);
			}
		}
	}
}

