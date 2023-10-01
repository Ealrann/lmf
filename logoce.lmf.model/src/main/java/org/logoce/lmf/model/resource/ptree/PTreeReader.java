package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.util.Tree;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public final class PTreeReader
{
	private final LMIterableLexer lexer = new LMIterableLexer();

	public Tree<List<PToken>> read(final InputStream inputStream)
	{
		final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		final var charSequence = reader.lines().collect(Collectors.joining("\n"));

		lexer.reset(charSequence, 0);

		final var buildState = new ReaderBuildState();
		lexer.forEach(buildState::readToken);
		return buildState.buildTree();
	}

	private static final class ReaderBuildState
	{
		private final Deque<PTreeBuilder> stack = new ArrayDeque<>();
		private final PTreeBuilder tree = new PTreeBuilder();

		public Tree<List<PToken>> buildTree()
		{
			return tree.build();
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
			final var newNode = stack.isEmpty() ? tree.newChild() : stack.getLast().newChild();
			stack.add(newNode);
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
