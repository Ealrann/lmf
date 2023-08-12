package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.EnumBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Enum<T> extends Datatype<T>
{
	List<String> literals();

	Group<Enum<?>> GROUP = LMCorePackage.ENUM_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	interface Features
	{
		Attribute<String, String> name = Named.Features.name;
		Attribute<String, List<String>> literals = new AttributeImpl<>("literals", true, true, false,
															   LMCorePackage.STRING_UNIT);

		List<Feature<?, ?>> All = List.of(name, literals);
	}

	static <T> Enum.Builder<T> builder() { return new EnumBuilder<>();}
	interface Builder<T> extends LMObject.Builder<Enum<T>>
	{
		Builder<T> name(String name);
		Builder<T> addLiteral(String literal);
	}
}
