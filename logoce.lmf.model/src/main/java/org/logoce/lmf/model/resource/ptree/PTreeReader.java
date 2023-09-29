package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.lexer.ELMTokenType;
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
	public Tree<List<PToken>> read(final InputStream inputStream)
	{
		final var lexer = new LMIterableLexer();
		final var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		final var charSequence = reader.lines().collect(Collectors.joining("\n"));

		lexer.reset(charSequence, 0);

		final var buildState = new ReaderBuildState();
		for (final PToken token : lexer)
		{
			final var type = token.type();
			if (type == ELMTokenType.OPEN_NODE) buildState.stack();
			else if (type == ELMTokenType.CLOSE_NODE) buildState.pop();
			else buildState.addToken(token);
		}

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

		public void stack()
		{
			final var newNode = stack.isEmpty() ? tree.newChild() : stack.getLast().newChild();
			stack.add(newNode);
		}

		public void pop()
		{
			stack.removeLast();
		}

		public void addToken(final PToken token)
		{
			if (!stack.isEmpty()) stack.getLast().addWord(token);
		}
	}
}
