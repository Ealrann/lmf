package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.resource.parsing.NodeParser;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.Tree;

import java.util.ArrayList;
import java.util.List;

public final class PTreeBuilder
{
	private final List<PToken> tokens = new ArrayList<>();
	private final List<PTreeBuilder> children = new ArrayList<>();
	private final NodeParser nodeParser;

	public PTreeBuilder(final NodeParser nodeParser)
	{
		this.nodeParser = nodeParser;
	}

	public Tree<PNode> build()
	{
		return build(null);
	}

	private Tree<PNode> build(final Tree<PNode> parent)
	{
		final var pnode = nodeParser.parse(tokens);
		return new Tree<>(parent, pnode, treeParent -> children.stream().map(c -> c.build(treeParent)).toList());
	}

	public void addWord(final PToken token)
	{
		tokens.add(token);
	}

	public PTreeBuilder newChild()
	{
		final var child = new PTreeBuilder(nodeParser);
		children.add(child);
		return child;
	}
}
