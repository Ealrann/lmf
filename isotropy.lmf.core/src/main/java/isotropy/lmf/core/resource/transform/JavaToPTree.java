package isotropy.lmf.core.resource.transform;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.resource.ptree.PTreeBuilder;
import isotropy.lmf.core.resource.util.Tree;

import java.util.List;

public final class JavaToPTree
{
	public Tree<List<String>> transform(final LMObject root)
	{
		final var treeBuidler = new PTreeBuilder();
		fill(treeBuidler, root);
		return treeBuidler.build();
	}

	private void fill(final PTreeBuilder treeBuilder, final LMObject object)
	{
		final var group = object.lmGroup();
		treeBuilder.addWord(group.name());

		// final var featureResolver = new FeatureToTreeResolver(object, group.features());
	}
}
