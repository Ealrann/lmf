package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GenericBuilder;

import java.util.function.Supplier;

public interface Generic extends Named
{
	Type type();
	BoundType boundType();

	static Builder builder() {return new GenericBuilder();}
	interface Builder extends LMObject.Builder<Generic>
	{
		Builder name(String name);
		Builder boundType(BoundType boundType);
		Builder type(Supplier<Type> type);
	}
}
