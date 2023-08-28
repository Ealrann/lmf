package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.EnumBuilder;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;

public interface Enum<T> extends Datatype<T>
{
	List<String> literals();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<String, List<String>> literals = new RawFeature<>(true,
																	 false,
																	 () -> LMCoreDefinition.Features.ENUM.literals);
	}

	static <T> Enum.Builder<T> builder() {return new EnumBuilder<>();}
	interface Builder<T> extends LMObject.Builder<Enum<T>>
	{
		Builder<T> name(String name);
		Builder<T> addLiteral(String literal);
	}
}
