package isotropy.lmf.core.lang;

import java.util.List;

public interface Type extends Named
{
	Group<Type> GROUP = LMCorePackage.TYPE_GROUP;

	interface Features
	{
		Attribute<String, String> Name = Named.Features.name;

		List<Feature<?, ?>> all = List.of(Name);
	}
}
