package org.logoce.lmf.model.util.tree;

import java.util.List;
import java.util.stream.Stream;

public interface StructuredTree<Y extends StructuredTree<Y>>
{
	Y parent();
	List<Y> children();
	@SuppressWarnings("unchecked")
	default Y root()
	{
		final var parent = parent();
		return parent == null ? (Y) this : parent.root();
	}
}
