package org.logoce.lmf.core.api.service;

import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.api.model.ModelRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * External loading facade for LMF.
 * <p>
 * This is the only loading entry point exposed from {@code logoce.lmf.core.api}.
 * The concrete implementation is provided by the {@code logoce.lmf.core.loader} module.
 */
public interface ILmLoader
{
	static Optional<ILmLoader> provider()
	{
		return ServiceLoader.load(ILmLoader.class).findFirst();
	}

	Session newSession(ModelRegistry registry);

	interface Session
	{
		List<? extends LMObject> loadObjects(InputStream inputStream) throws IOException, LmLoadException;
	}
}
