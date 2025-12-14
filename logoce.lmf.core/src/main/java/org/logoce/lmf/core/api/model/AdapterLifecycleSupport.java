package org.logoce.lmf.core.api.model;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.util.oldlogoce.TreeLazyIterator;

/**
 * Internal helper for adapter lifecycle traversal.
 */
final class AdapterLifecycleSupport
{
	private AdapterLifecycleSupport()
	{
	}

	static void loadExtenderManager(final FeaturedObject<?> root)
	{
		treeIterator(root).forEachRemaining(object -> loadNode((FeaturedObject<?>) object));
	}

	static void disposeExtenderManager(final FeaturedObject<?> root)
	{
		treeIterator(root).forEachRemaining(object -> disposeNode((FeaturedObject<?>) object));
	}

	static TreeLazyIterator treeIterator(final FeaturedObject<?> root)
	{
		return new TreeLazyIterator((LMObject) root);
	}

	private static void loadNode(final FeaturedObject<?> object)
	{
		if (!object.loaded)
		{
			object.loaded = true;
			object.adapterManager().load();
		}
	}

	private static void disposeNode(final FeaturedObject<?> object)
	{
		if (object.loaded)
		{
			object.adapterManager().dispose();
			object.loaded = false;
		}
	}
}
