package org.logoce.lmf.core.util;

import org.logoce.lmf.core.lang.Model;

import java.util.stream.Stream;

public interface IModelRegistry
{
	Model getModel(final String qualifiedName);

	Model getModel(final String domain, final String name);

	Stream<Model> models();
}
