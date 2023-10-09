package org.logoce.lmf.model.resource;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.resource.transform.PModelBuilder;

import java.io.InputStream;
import java.util.List;

public final class ResourceUtil
{
	private static final PModelBuilder PMODEL_BUILDER = new PModelBuilder();

	public static List<? extends LMObject> loadModel(final InputStream inputStream)
	{
		final var ptreeReader = new PTreeReader();
		final var roots = ptreeReader.read(inputStream);

		return PMODEL_BUILDER.build(roots);
	}
}
