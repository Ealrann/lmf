package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.util.oldlogoce.TreeLazyIterator;

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
		return new TreeLazyIterator((org.logoce.lmf.model.lang.LMObject) root);
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
