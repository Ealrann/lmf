package org.logoce.lmf.core.loader.service;

import org.logoce.lmf.core.api.service.ILmLoader;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.api.service.LmLoadException;
import org.logoce.lmf.core.lang.LMObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public final class LMLoader implements ILmLoader
{
	@Override
	public Session newSession(final ModelRegistry registry)
	{
		Objects.requireNonNull(registry, "registry");
		return new SessionImpl(registry);
	}

	private static final class SessionImpl implements Session
	{
		private final LmLoader loader;

		private SessionImpl(final ModelRegistry registry)
		{
			this.loader = new LmLoader(registry);
		}

		@Override
		public List<? extends LMObject> loadObjects(final InputStream inputStream) throws IOException, LmLoadException
		{
			try
			{
				return loader.loadObjects(inputStream);
			}
			catch (final RuntimeException e)
			{
				final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
				throw new LmLoadException(message, e);
			}
		}
	}
}
