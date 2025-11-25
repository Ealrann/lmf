package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.tree.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class TokenLocator
{
	private TokenLocator()
	{
	}

	static List<TokenHit> findToken(final List<File> modelFiles, final String tokenValue)
	{
		final List<TokenHit> hits = new ArrayList<>();
		final var reader = new PTreeReader();

		for (final var file : modelFiles)
		{
			try (final var inputStream = new FileInputStream(file))
			{
				final var readResult = reader.readWithDiagnostics(inputStream, new ArrayList<>());
				for (final var root : readResult.model())
				{
					collectTokens(root, readResult.source(), file, tokenValue, hits);
				}
			}
			catch (IOException ignored)
			{
			}
		}

		return hits;
	}

	static Optional<TokenHit> findFirst(final List<File> modelFiles, final String tokenValue)
	{
		final var reader = new PTreeReader();
		for (final var file : modelFiles)
		{
			try (final var inputStream = new FileInputStream(file))
			{
				final var readResult = reader.readWithDiagnostics(inputStream, new ArrayList<>());
				for (final var root : readResult.model())
				{
					final var hit = firstToken(root, readResult.source(), file, tokenValue);
					if (hit.isPresent()) return hit;
				}
			}
			catch (IOException ignored)
			{
			}
		}
		return Optional.empty();
	}

	private static Optional<TokenHit> firstToken(final Tree<PNode> node,
												final CharSequence source,
												final File file,
												final String tokenValue)
	{
		for (final PToken token : node.data().tokens())
		{
			if (tokenValue.equals(token.value()))
			{
				final var span = spanOf(token, source);
				return Optional.of(new TokenHit(file, span.line(), span.column(), tokenValue));
			}
		}

		for (final var child : node.children())
		{
			final var hit = firstToken(child, source, file, tokenValue);
			if (hit.isPresent()) return hit;
		}
		return Optional.empty();
	}

	private static void collectTokens(final Tree<PNode> node,
									  final CharSequence source,
									  final File file,
									  final String tokenValue,
									  final List<TokenHit> hits)
	{
		for (final PToken token : node.data().tokens())
		{
			if (tokenValue.equals(token.value()))
			{
				final var span = spanOf(token, source);
				hits.add(new TokenHit(file, span.line(), span.column(), tokenValue));
			}
		}

		for (final var child : node.children())
		{
			collectTokens(child, source, file, tokenValue, hits);
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

	record TokenHit(File file, int line, int column, String token)
	{}
}
