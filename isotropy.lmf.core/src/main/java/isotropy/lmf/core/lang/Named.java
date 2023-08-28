package isotropy.lmf.core.lang;

import isotropy.lmf.core.model.RawFeature;

public interface Named extends LMObject
{
	String name();

	interface Features
	{
		RawFeature<String, String> name = new RawFeature<>(false, false, () -> LMCoreDefinition.Features.NAMED.name);
	}
}
