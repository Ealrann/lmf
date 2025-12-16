package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.core.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.text.syntax.PToken;
import org.logoce.lmf.core.api.util.TextPositions;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
		final var reader = new LmTreeReader();

		for (final var file : modelFiles)
		{
			try
			{
				final var source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
				final var readResult = reader.read(source);
				for (final var root : readResult.roots())
				{
					collectTokens(root, source, file, tokenValue, hits);
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
		final var reader = new LmTreeReader();
		for (final var file : modelFiles)
		{
			try
			{
				final var source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
				final var readResult = reader.read(source);
				for (final var root : readResult.roots())
				{
					final var hit = firstToken(root, source, file, tokenValue);
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
				final var span = TextPositions.spanOf(token, source);
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
				final var span = TextPositions.spanOf(token, source);
				hits.add(new TokenHit(file, span.line(), span.column(), tokenValue));
			}
		}

		for (final var child : node.children())
		{
			collectTokens(child, source, file, tokenValue, hits);
		}
	}

	record TokenHit(File file, int line, int column, String token)
	{}
}
