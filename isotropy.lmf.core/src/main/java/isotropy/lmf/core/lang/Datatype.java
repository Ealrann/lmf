package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Datatype<T> extends Type
{
	Group<Datatype<?>> GROUP = LMCorePackage.DATATYPE_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	interface Features
	{
		Attribute<String, String> name = Type.Features.Name;

		List<Feature<?, ?>> All = List.of(name);
	}
}
