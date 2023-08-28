package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ReferenceBuilder;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;
import java.util.function.Supplier;

public interface Reference<T extends LMObject> extends LMObject
{
	Concept<T> group();

	List<? extends Concept<?>> parameters();

	interface Features
	{
		RawFeature<Concept<?>, Concept<?>> group = new RawFeature<>(false,
																	true,
																	() -> LMCoreDefinition.Features.REFERENCE.group);
		RawFeature<Concept<?>, List<Concept<?>>> parameters = new RawFeature<>(true,
																			   true,
																			   () -> LMCoreDefinition.Features.REFERENCE.parameters);
	}

	static <T extends LMObject> Builder<T> builder() {return new ReferenceBuilder<>();}
	interface Builder<UnaryType extends LMObject> extends LMObject.Builder<Reference<UnaryType>>
	{
		Builder<UnaryType> group(Supplier<Concept<UnaryType>> group);
		Builder<UnaryType> addParameter(Supplier<Concept<?>> parameter);
	}
}
