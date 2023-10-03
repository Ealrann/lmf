package org.logoce.lmf.model.resource;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.ptree.PTreeReader;
import org.logoce.lmf.model.resource.transform.PTreeToJava;

import java.io.InputStream;
import java.util.List;

public final class ResourceUtil
{
	public static List<? extends LMObject> loadModel(final InputStream inputStream)
	{
		final var ptreeReader = new PTreeReader();
		final var roots = ptreeReader.read(inputStream);

		final var modelBuilder = new PTreeToJava();
		return modelBuilder.transform(roots);
	}
}
