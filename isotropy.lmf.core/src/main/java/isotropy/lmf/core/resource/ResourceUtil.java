package isotropy.lmf.core.resource;

import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.resource.ptree.PTreeReader;
import isotropy.lmf.core.resource.transform.PTreeToJava;

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
