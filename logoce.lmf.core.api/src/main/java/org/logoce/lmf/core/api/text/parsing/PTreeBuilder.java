package org.logoce.lmf.core.api.text.parsing;

import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.text.syntax.PToken;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;

public final class PTreeBuilder
{
	private final List<PToken> tokens = new ArrayList<>();
	private final List<PTreeBuilder> children = new ArrayList<>();

	public PTreeBuilder()
	{}

	public Tree<PNode> build()
	{
		return build(null);
	}

	private Tree<PNode> build(final Tree<PNode> parent)
	{
		final var pnode = new PNode.SimplePNode(tokens);
		return new Tree<>(parent, pnode, treeParent -> children.stream().map(c -> c.build(treeParent)).toList());
	}

	public void addWord(final PToken token)
	{
		tokens.add(token);
	}

	public PTreeBuilder newChild()
	{
		final var child = new PTreeBuilder();
		children.add(child);
		return child;
	}
}
