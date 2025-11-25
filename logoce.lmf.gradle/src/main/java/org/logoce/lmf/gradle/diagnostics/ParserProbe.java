package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.model.resource.diagnostic.ParseDiagnostic;
import org.logoce.lmf.model.resource.diagnostic.ParseDiagnostic.Severity;
import org.logoce.lmf.model.resource.parsing.LMIterableLexer;
import org.logoce.lmf.model.resource.parsing.PToken;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

final class ParserProbe
{
	private ParserProbe()
	{
	}

	static Optional<ParseDiagnostic> probe(final File file, final Exception originalError)
	{
		PToken lastToken = null;

		try
		{
			final var source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			final var lexer = new LMIterableLexer();
			lexer.reset(source, 0);

			for (final var token : lexer)
			{
				lastToken = token;
			}
			// If we get here, lexing succeeded; nothing to add.
			return Optional.empty();
		}
		catch (Exception lexError)
		{
			final String baseMessage = originalError.getMessage() == null ? originalError.getClass().getSimpleName() :
									   originalError.getMessage();
			final String message = lastToken == null
								   ? baseMessage
								   : baseMessage + " (stopped after token '" + lastToken.value() + "')";

			if (lastToken == null)
			{
				return Optional.of(new ParseDiagnostic(1, 1, 1, 0, Severity.ERROR, message));
			}

			final var source = safeRead(file);
			final var span = spanOf(lastToken, source);
			return Optional.of(new ParseDiagnostic(span.line(), span.column(), Math.max(1, lastToken.length()),
												  lastToken.offset(), Severity.ERROR, message));
		}
	}

	private static String safeRead(final File file)
	{
		try
		{
			return Files.readString(file.toPath(), StandardCharsets.UTF_8);
		}
		catch (IOException e)
		{
			return "";
		}
	}

	private static Span spanOf(final PToken token, final CharSequence source)
	{
		final int offset = token.offset();
		int line = 1;
		int col = 1;
		for (int i = 0; i < offset && i < source.length(); i++)
		{
			if (source.charAt(i) == '\n')
			{
				line++;
				col = 1;
			}
			else
			{
				col++;
			}
		}
		return new Span(line, col);
	}

	private record Span(int line, int column)
	{}
}
