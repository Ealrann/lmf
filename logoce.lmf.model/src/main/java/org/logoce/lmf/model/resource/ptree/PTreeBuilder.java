package org.logoce.lmf.model.resource.ptree;

import org.logoce.lmf.model.resource.util.Tree;

import java.util.ArrayList;
import java.util.List;

public final class PTreeBuilder
{
	private final List<String> words = new ArrayList<>();
	private final List<PTreeBuilder> children = new ArrayList<>();

	public PTreeBuilder() {}

	public PTreeBuilder(final Tree<List<String>> tree)
	{
		words.addAll(tree.data());
		tree.children()
			.stream()
			.map(PTreeBuilder::new)
			.forEach(children::add);
	}

	public Tree<List<String>> build()
	{
		return build(null);
	}

	public Tree<List<String>> build(final Tree<List<String>> parent)
	{
		return new Tree<>(parent,
						  words,
						  treeParent -> children.stream()
												.map(c -> c.build(treeParent))
												.toList());
	}

	public void addWord(final String word)
	{
		words.add(word);
	}

	public void mergeWithLastWord(final String word)
	{
		final int lastIndex = words.size() - 1;
		words.set(lastIndex, words.get(lastIndex) + word);
	}

	public PTreeBuilder newChild()
	{
		final var child = new PTreeBuilder();
		children.add(child);
		return child;
	}
}
