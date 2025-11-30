package org.logoce.lmf.model.resource;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.loader.model.LmDocument;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public final class ResourceUtil
{
	public static List<? extends LMObject> loadObject(final InputStream inputStream, final ModelRegistry modelRegistry)
	{
		try
		{
			final var loader = new LmLoader(modelRegistry);
			return loader.loadObjects(inputStream);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read LM model", e);
		}
	}

	public static Model loadModel(final InputStream inputStream, final ModelRegistry modelRegistry)
	{
		try
		{
			final var loader = new LmLoader(modelRegistry);
			final LmDocument doc = loader.loadModel(inputStream);
			final var model = doc.model();
			if (model instanceof Model m)
			{
				return m;
			}
			throw new IllegalArgumentException("This input doesn't define a valid model. Use loadObject() instead.");
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read LM model", e);
		}
	}

	public static List<Model> loadModels(final List<InputStream> inputStreams, final ModelRegistry modelRegistry)
	{
		try
		{
			final var loader = new LmLoader(modelRegistry);
			return loader.loadModels(inputStreams);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read LM models", e);
		}
	}
}
