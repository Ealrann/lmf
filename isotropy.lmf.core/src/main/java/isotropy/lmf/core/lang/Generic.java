package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GenericBuilder;

import java.util.function.Supplier;

public interface Generic<T> extends Concept<T>
{
	Type<T> type();
	BoundType boundType();

	static <T> Builder<T> builder() {return new GenericBuilder<>();}
	interface Builder<T> extends LMObject.Builder<Generic<T>>
	{
		Builder<T> name(String name);
		Builder<T> boundType(BoundType boundType);
		Builder<T> type(Supplier<Type<T>> type);
	}
}
