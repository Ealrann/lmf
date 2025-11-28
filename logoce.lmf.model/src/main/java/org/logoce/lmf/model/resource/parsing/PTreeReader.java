package org.logoce.lmf.model.resource.parsing;

import org.logoce.lmf.model.util.tree.Tree;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PTreeReader
{
	private final LMIterableLexer lexer = new LMIterableLexer();

	public PTreeReader()
	{
	}

	public List<Tree<PNode>> read(final InputStream inputStream)
	{
		return readWithDiagnostics(inputStream, new ArrayList<>()).model();
	}

	public ReadResult readWithDiagnostics(final InputStream inputStream,
										  final List<ParseDiagnostic> diagnostics)
	{
		final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		final var source = reader.lines().collect(Collectors.joining("\n"));

		int lastOffset = 0;
		int lastLength = 1;
		try
		{
			lexer.reset(source, 0);
			final var modelBuilder = new PNodeBuilder();
			for (PToken token : lexer)
			{
				lastOffset = token.offset();
				lastLength = token.length();
				modelBuilder.readToken(token);
			}
			return new ReadResult(modelBuilder.buildRoots(), diagnostics, source);
		}
		catch (LexerException e)
		{
			final int offset = Math.min(source.length(), Math.max(0, lastOffset));
			final int line = lineFor(source, offset);
			final int col = columnFor(source, offset);
			diagnostics.add(new ParseDiagnostic(
				line, col, Math.max(1, lastLength), offset, ParseDiagnostic.Severity.ERROR,
				e.getMessage() == null ? "Parse error" : e.getMessage()));
			return new ReadResult(List.of(), diagnostics, source);
		}
	}

	public record ReadResult(List<Tree<PNode>> model, List<ParseDiagnostic> diagnostics, CharSequence source) {}

	private static int lineFor(CharSequence text, int offset) {
		int line = 1;
		for (int i = 0; i < offset && i < text.length(); i++) {
			if (text.charAt(i) == '\n') line++;
		}
		return line;
	}

	private static int columnFor(CharSequence text, int offset) {
		int col = 1;
		for (int i = offset - 1; i >= 0 && i < text.length(); i--) {
			if (text.charAt(i) == '\n') break;
			col++;
		}
		return col;
	}
}
