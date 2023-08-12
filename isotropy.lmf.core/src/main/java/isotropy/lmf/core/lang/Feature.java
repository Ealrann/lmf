package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Feature<UnaryType, EffectiveType> extends Named
{
	boolean immutable();
	boolean many();
	boolean mandatory();

	Group<Feature<?, ?>> GROUP = LMCorePackage.FEATURE_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	interface Features
	{
		Attribute<String, String> name = Named.Features.name;
		Attribute<Boolean, Boolean> immutable = new AttributeImpl<>("immutable",
																	true,
																	false,
																	false,
																	LMCorePackage.BOOLEAN_UNIT);
		Attribute<Boolean, Boolean> many = new AttributeImpl<>("many", true, false, false, LMCorePackage.BOOLEAN_UNIT);
		Attribute<Boolean, Boolean> mandatory = new AttributeImpl<>("mandatory",
																	true,
																	false,
																	false,
																	LMCorePackage.BOOLEAN_UNIT);

		List<Feature<?, ?>> All = List.of(name, immutable, many, mandatory);
	}
}
