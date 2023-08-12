package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.AttributeImpl;

import java.util.List;

public interface Named extends LMObject
{
	String name();

	interface Features
	{
		Attribute<String, String> name = new AttributeImpl<>("name", true, false, true, LMCorePackage.STRING_UNIT);

		List<Feature<?, ?>> all = List.of(name);
	}
}
