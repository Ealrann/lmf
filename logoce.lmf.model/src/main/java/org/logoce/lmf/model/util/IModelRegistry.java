package org.logoce.lmf.model.util;

import org.logoce.lmf.model.lang.Model;

import java.util.stream.Stream;

public interface IModelRegistry
{
	Model getModel(final String name);

	Stream<Model> models();
}
