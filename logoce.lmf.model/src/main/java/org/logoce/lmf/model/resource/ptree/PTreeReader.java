package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.resource.parsing.NodeParser;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.Tree;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PTreeReader
{
	private final LMIterableLexer lexer = new LMIterableLexer();
	private final NodeParser nodeParser;

	public PTreeReader()
	{
		nodeParser = new NodeParser(ModelRegistry.Instance.getAliasMap());
	}

	public List<Tree<PNode>> read(final InputStream inputStream)
	{
		final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		final var charSequence = reader.lines().collect(Collectors.joining("\n"));

		try
		{
			lexer.reset(charSequence, 0);
			final var buildState = new PModelBuilder(nodeParser);
			for (PToken token : lexer)
			{
				buildState.readToken(token);
			}
			return buildState.buildTrees();
		}
		catch (LexerException e)
		{
			e.printStackTrace();
			return List.of();
		}
	}
}
