package isotropy.lmf.core.lang;

import isotropy.lmf.core.model.RawFeature;

public interface Datatype<T> extends Type<T>
{
	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
	}
}
