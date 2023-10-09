package org.logoce.lmf.model.resource.parsing;

import org.logoce.lmf.model.util.Tree;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
		final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		final var charSequence = reader.lines().collect(Collectors.joining("\n"));

		try
		{
			lexer.reset(charSequence, 0);
			final var modelBuilder = new PModelBuilder();
			for (PToken token : lexer)
			{
				modelBuilder.readToken(token);
			}
			return modelBuilder.buildRoots();
		}
		catch (LexerException e)
		{
			e.printStackTrace();
			return List.of();
		}
	}
}
