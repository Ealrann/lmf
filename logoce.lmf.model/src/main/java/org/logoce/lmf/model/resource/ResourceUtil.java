package org.logoce.lmf.model.resource;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.resource.transform.multi.MultiModelLoader;
import org.logoce.lmf.model.resource.transform.PModelLinker;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public final class ResourceUtil
{
	private static final PModelLinker<PNode> PMODEL_BUILDER = new PModelLinker<>();

	public static List<? extends LMObject> loadObject(final InputStream inputStream)
	{
		final var ptreeReader = new PTreeReader();
		final var roots = ptreeReader.read(inputStream);
		return PMODEL_BUILDER.build(roots);
	}

	public static Model loadModel(final InputStream inputStream)
	{
		final var ptreeReader = new PTreeReader();
		final var roots = ptreeReader.read(inputStream);
		final var root = PMODEL_BUILDER.build(roots).get(0);
		if (root instanceof Model model) return model;
		else throw new IllegalArgumentException("This input doesn't define a valid model. Use loadObject() instead.");
	}

	public static List<Model> loadModels(final List<InputStream> inputStreams)
	{
		final var ptreeReader = new PTreeReader();
		final var pTress = inputStreams.stream().map(ptreeReader::read).flatMap(Collection::stream).toList();
		final var modelLoader = new MultiModelLoader(pTress);
		return modelLoader.build(PMODEL_BUILDER);
	}
}
