package org.logoce.lmf.cli.remove;

import org.logoce.lmf.cli.ref.ObjectId;
import org.logoce.lmf.core.loader.api.loader.linking.tree.LinkNodeInternal;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;

import java.util.HashSet;
import java.util.Set;

final class RemoveNodeCollector
{
	Set<LinkNodeInternal<?, PNode, ?>> collectNodes(final LinkNodeInternal<?, PNode, ?> root)
	{
		final var nodes = new HashSet<LinkNodeInternal<?, PNode, ?>>();
		root.streamTree().forEach(nodes::add);
		return Set.copyOf(nodes);
	}

	Set<ObjectId> collectObjectIds(final Set<LinkNodeInternal<?, PNode, ?>> nodes)
	{
		final var ids = new HashSet<ObjectId>();
		for (final var node : nodes)
		{
			final var id = resolveObjectId(node, "removed subtree");
			if (id != null)
			{
				ids.add(id);
			}
		}
		return Set.copyOf(ids);
	}

	ObjectId resolveObjectId(final LinkNodeInternal<?, PNode, ?> node, final String label)
	{
		try
		{
			final var built = node.build();
			final var id = ObjectId.from(built);
			if (id == null)
			{
				throw new RemovePlanException("Cannot resolve " + label + " object id");
			}
			return id;
		}
		catch (RuntimeException e)
		{
			throw new RemovePlanException("Cannot resolve " + label + " object id");
		}
	}
}
