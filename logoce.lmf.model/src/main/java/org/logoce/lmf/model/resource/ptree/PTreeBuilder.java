package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.util.Tree;

import java.util.ArrayList;
import java.util.List;

public final class PTreeBuilder
{
	private final List<PToken> tokens = new ArrayList<>();
	private final List<PTreeBuilder> children = new ArrayList<>();

	public PTreeBuilder() {}

	public Tree<List<PToken>> build()
	{
		return build(null);
	}

	private Tree<List<PToken>> build(final Tree<List<PToken>> parent)
	{
		return new Tree<>(parent, tokens, treeParent -> children.stream().map(c -> c.build(treeParent)).toList());
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
