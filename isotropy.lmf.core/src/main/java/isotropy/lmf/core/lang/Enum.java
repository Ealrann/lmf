package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.EnumBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Enum<T> extends Datatype<T>
{
	List<String> literals();

	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	static <T> Enum.Builder<T> builder() {return new EnumBuilder<>();}
	interface Builder<T> extends LMObject.Builder<Enum<T>>
	{
		Builder<T> name(String name);
		Builder<T> addLiteral(String literal);
	}
}
