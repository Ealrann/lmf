package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GenericBuilder;
import isotropy.lmf.core.model.RawFeature;

import java.util.function.Supplier;

public interface Generic<T> extends Concept<T>
{
	Type<T> type();
	BoundType boundType();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<Type<?>, Type<?>> type = new RawFeature<>(true, false, () -> LMCoreDefinition.Features.GENERIC.type);
		RawFeature<BoundType, BoundType> boundType = new RawFeature<>(true,
																	  false,
																	  () -> LMCoreDefinition.Features.GENERIC.boundType);
	}

	static <T> Builder<T> builder() {return new GenericBuilder<>();}
	interface Builder<T> extends LMObject.Builder<Generic<T>>
	{
		Builder<T> name(String name);
		Builder<T> boundType(BoundType boundType);
		Builder<T> type(Supplier<Type<T>> type);
	}
}
