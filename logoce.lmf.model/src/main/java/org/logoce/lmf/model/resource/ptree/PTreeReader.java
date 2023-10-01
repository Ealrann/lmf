package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.api.model.IModelPackage;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PTreeReader
{
	private final LMIterableLexer lexer = new LMIterableLexer();
	private final NodeParser nodeParser;

	public PTreeReader()
	{
		final var aliases = ModelRegistry.Instance.models()
												  .map(IModelPackage::model)
												  .map(Model::aliases)
												  .flatMap(Collection::stream)
												  .collect(Collectors.toUnmodifiableMap(Named::name,
																						Function.identity()));

		nodeParser = new NodeParser(aliases);
	}

	public List<Tree<PNode>> read(final InputStream inputStream)
	{
		final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		final var charSequence = reader.lines().collect(Collectors.joining("\n"));

		lexer.reset(charSequence, 0);

		final var buildState = new ReaderBuildState(nodeParser);
		lexer.forEach(buildState::readToken);
		return buildState.buildTrees();
	}

	private static final class ReaderBuildState
	{
		private final Deque<PTreeBuilder> stack = new ArrayDeque<>();
		private final List<PTreeBuilder> roots = new ArrayList<>();
		private final NodeParser nodeParser;

		public ReaderBuildState(final NodeParser nodeParser)
		{
			this.nodeParser = nodeParser;
		}

		public List<Tree<PNode>> buildTrees()
		{
			return roots.stream().map(PTreeBuilder::build).toList();
		}

		public void readToken(final PToken token)
		{
			switch (token.type())
			{
				case OPEN_NODE -> stack();
				case CLOSE_NODE -> pop();
				default -> addToken(token);
			}
		}

		private void stack()
		{
			final var newNode = stack.isEmpty() ? newRoot() : stack.getLast().newChild();
			stack.add(newNode);
		}

		private PTreeBuilder newRoot()
		{
			final var root = new PTreeBuilder(nodeParser);
			roots.add(root);
			return root;
		}

		private void pop()
		{
			stack.removeLast();
		}

		private void addToken(final PToken token)
		{
			if (!stack.isEmpty()) stack.getLast().addWord(token);
		}
	}
}
