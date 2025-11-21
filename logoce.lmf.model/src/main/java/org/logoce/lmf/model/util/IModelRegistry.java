package org.logoce.lmf.model.util;

import org.logoce.lmf.model.lang.Model;

import java.util.stream.Stream;

public interface IModelRegistry
{
	Model getModel(final String qualifiedName);

	Model getModel(final String domain, final String name);

	Stream<Model> models();
}
