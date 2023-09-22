package logoce.lmf.model.resource;

import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.resource.ptree.PTreeReader;
import logoce.lmf.model.resource.transform.PTreeToJava;

import java.io.InputStream;
import java.util.List;

public class ResourceUtil
{
	public static final List<? extends LMObject> loadModel(final InputStream inputStream)
	{
		final var ptreeBuilder = new PTreeReader();
		final var ptree = ptreeBuilder.read(inputStream);

		final var modelBuilder = new PTreeToJava();
		final var roots = modelBuilder.transform(ptree);

		return roots;
	}
}
