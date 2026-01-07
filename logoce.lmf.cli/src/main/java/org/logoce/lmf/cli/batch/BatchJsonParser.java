package org.logoce.lmf.cli.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class BatchJsonParser
{
	private BatchJsonParser()
	{
	}

	static ParsedOperation parseOperation(final String line, final int lineNumber)
	{
		Objects.requireNonNull(line, "line");
		if (!line.startsWith("{") || !line.endsWith("}"))
		{
			throw new BatchParseException(lineNumber, "Expected a JSON object line like {\"cmd\":\"rename\",\"args\":[...] }");
		}

		final var scanner = new Scanner(line, lineNumber);
		scanner.expect('{');

		String cmd = null;
		List<String> args = List.of();

		scanner.skipWhitespace();
		while (!scanner.peek('}'))
		{
			final var key = scanner.readString();
			scanner.skipWhitespace();
			scanner.expect(':');
			scanner.skipWhitespace();

			if ("cmd".equals(key))
			{
				cmd = scanner.readString();
			}
			else if ("args".equals(key))
			{
				args = scanner.readStringArray();
			}
			else
			{
				scanner.skipValue();
			}

			scanner.skipWhitespace();
			if (scanner.peek(','))
			{
				scanner.expect(',');
				scanner.skipWhitespace();
			}
			else
			{
				break;
			}
		}

		scanner.skipWhitespace();
		scanner.expect('}');
		scanner.skipWhitespace();
		if (!scanner.eof())
		{
			throw new BatchParseException(lineNumber, "Unexpected trailing characters");
		}

		if (cmd == null || cmd.isBlank())
		{
			throw new BatchParseException(lineNumber, "Missing required JSON field: \"cmd\"");
		}

		return new ParsedOperation(cmd.strip(), args == null ? List.of() : List.copyOf(args));
	}

	record ParsedOperation(String command, List<String> args)
	{
	}

	static final class BatchParseException extends RuntimeException
	{
		private final int lineNumber;

		BatchParseException(final int lineNumber, final String message)
		{
			super(message);
			this.lineNumber = lineNumber;
		}

		int lineNumber()
		{
			return lineNumber;
		}
	}

	private static final class Scanner
	{
		private final String text;
		private final int lineNumber;
		private int index;

		private Scanner(final String text, final int lineNumber)
		{
			this.text = text;
			this.lineNumber = lineNumber;
		}

		void skipWhitespace()
		{
			while (index < text.length() && Character.isWhitespace(text.charAt(index)))
			{
				index++;
			}
		}

		boolean eof()
		{
			return index >= text.length();
		}

		boolean peek(final char c)
		{
			return index < text.length() && text.charAt(index) == c;
		}

		void expect(final char c)
		{
			if (index >= text.length() || text.charAt(index) != c)
			{
				throw new BatchParseException(lineNumber, "Expected '" + c + "'");
			}
			index++;
		}

		String readString()
		{
			if (!peek('"'))
			{
				throw new BatchParseException(lineNumber, "Expected JSON string");
			}
			expect('"');

			final var out = new StringBuilder();
			while (index < text.length())
			{
				final char c = text.charAt(index++);
				if (c == '"')
				{
					return out.toString();
				}
				if (c == '\\')
				{
					if (index >= text.length())
					{
						throw new BatchParseException(lineNumber, "Unterminated escape sequence");
					}
					final char esc = text.charAt(index++);
					out.append(switch (esc)
							   {
								   case '"', '\\', '/' -> esc;
								   case 'b' -> '\b';
								   case 'f' -> '\f';
								   case 'n' -> '\n';
								   case 'r' -> '\r';
								   case 't' -> '\t';
								   case 'u' -> readUnicodeEscape();
								   default -> throw new BatchParseException(lineNumber, "Unsupported escape sequence: \\" + esc);
							   });
					continue;
				}
				out.append(c);
			}

			throw new BatchParseException(lineNumber, "Unterminated JSON string");
		}

		private char readUnicodeEscape()
		{
			if (index + 4 > text.length())
			{
				throw new BatchParseException(lineNumber, "Invalid unicode escape");
			}

			int codePoint = 0;
			for (int i = 0; i < 4; i++)
			{
				final char c = text.charAt(index++);
				codePoint <<= 4;
				if (c >= '0' && c <= '9')
				{
					codePoint |= (c - '0');
				}
				else if (c >= 'a' && c <= 'f')
				{
					codePoint |= (c - 'a') + 10;
				}
				else if (c >= 'A' && c <= 'F')
				{
					codePoint |= (c - 'A') + 10;
				}
				else
				{
					throw new BatchParseException(lineNumber, "Invalid unicode escape");
				}
			}
			return (char) codePoint;
		}

		List<String> readStringArray()
		{
			if (!peek('['))
			{
				throw new BatchParseException(lineNumber, "Expected JSON array");
			}
			expect('[');
			skipWhitespace();

			final var values = new ArrayList<String>();
			if (peek(']'))
			{
				expect(']');
				return List.copyOf(values);
			}

			while (true)
			{
				skipWhitespace();
				values.add(readString());
				skipWhitespace();

				if (peek(','))
				{
					expect(',');
					continue;
				}
				if (peek(']'))
				{
					expect(']');
					break;
				}
				throw new BatchParseException(lineNumber, "Expected ',' or ']'");
			}

			return List.copyOf(values);
		}

		void skipValue()
		{
			skipWhitespace();
			if (eof())
			{
				throw new BatchParseException(lineNumber, "Unexpected end of JSON input");
			}

			final char c = text.charAt(index);
			if (c == '"')
			{
				readString();
				return;
			}
			if (c == '[')
			{
				skipArray();
				return;
			}
			if (c == '{')
			{
				skipObject();
				return;
			}

			skipLiteralOrNumber();
		}

		private void skipArray()
		{
			expect('[');
			int depth = 1;
			boolean inString = false;
			boolean escaped = false;

			while (index < text.length())
			{
				final char c = text.charAt(index++);
				if (inString)
				{
					if (escaped)
					{
						escaped = false;
						continue;
					}
					if (c == '\\')
					{
						escaped = true;
						continue;
					}
					if (c == '"')
					{
						inString = false;
					}
					continue;
				}

				if (c == '"')
				{
					inString = true;
				}
				else if (c == '[')
				{
					depth++;
				}
				else if (c == ']')
				{
					depth--;
					if (depth == 0)
					{
						return;
					}
				}
			}

			throw new BatchParseException(lineNumber, "Unterminated JSON array");
		}

		private void skipObject()
		{
			expect('{');
			int depth = 1;
			boolean inString = false;
			boolean escaped = false;

			while (index < text.length())
			{
				final char c = text.charAt(index++);
				if (inString)
				{
					if (escaped)
					{
						escaped = false;
						continue;
					}
					if (c == '\\')
					{
						escaped = true;
						continue;
					}
					if (c == '"')
					{
						inString = false;
					}
					continue;
				}

				if (c == '"')
				{
					inString = true;
				}
				else if (c == '{')
				{
					depth++;
				}
				else if (c == '}')
				{
					depth--;
					if (depth == 0)
					{
						return;
					}
				}
			}

			throw new BatchParseException(lineNumber, "Unterminated JSON object");
		}

		private void skipLiteralOrNumber()
		{
			while (index < text.length())
			{
				final char c = text.charAt(index);
				if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c))
				{
					return;
				}
				index++;
			}
		}
	}
}

