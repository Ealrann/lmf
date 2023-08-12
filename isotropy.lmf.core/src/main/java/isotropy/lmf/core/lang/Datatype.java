package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Datatype<T> extends Type
{
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));
}
