package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ReferenceBuilder;

import java.util.List;
import java.util.function.Supplier;

public interface Reference<T extends LMObject> extends LMObject
{
	Concept<T> group();

	List<? extends Concept<?>> parameters();

	static <T extends LMObject> Builder<T> builder() {return new ReferenceBuilder<>();}
	interface Builder<UnaryType extends LMObject> extends LMObject.Builder<Reference<UnaryType>>
	{
		Builder<UnaryType> group(Supplier<Concept<UnaryType>> group);
		Builder<UnaryType> addParameter(Supplier<Concept<?>> parameter);
	}
}
