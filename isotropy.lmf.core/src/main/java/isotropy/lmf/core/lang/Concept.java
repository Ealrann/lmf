package isotropy.lmf.core.lang;

import isotropy.lmf.core.model.RawFeature;

public interface Concept<T> extends Named
{
	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
	}
}
