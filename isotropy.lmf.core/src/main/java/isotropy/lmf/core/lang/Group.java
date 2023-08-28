package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GroupBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;
import java.util.function.Supplier;

public interface Group<T extends LMObject> extends Type<T>, Concept<T>
{
	boolean concrete();
	List<? extends Reference<?>> includes();
	List<? extends Feature<?, ?>> features();
	List<? extends Generic<?>> generics();
	List<? extends Generic<?>> parameters();

	List<Generic<?>> GENERICS = List.of(new GenericImpl<>("T", BoundType.Extends, LMCoreDefinition.Groups.LM_OBJECT));

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<Boolean, Boolean> concrete = new RawFeature<>(false,
																 false,
																 () -> LMCoreDefinition.Features.GROUP.concrete);
		RawFeature<Reference<?>, List<? extends Reference<?>>> includes = new RawFeature<>(true,
																						   true,
																						   () -> LMCoreDefinition.Features.GROUP.includes);
		RawFeature<Feature<?, ?>, List<? extends Feature<?, ?>>> features = new RawFeature<>(true,
																							 true,
																							 () -> LMCoreDefinition.Features.GROUP.features);
		RawFeature<Generic<?>, List<? extends Generic<?>>> generics = new RawFeature<>(true,
																					   true,
																					   () -> LMCoreDefinition.Features.GROUP.generics);
		RawFeature<Generic<?>, List<? extends Generic<?>>> parameters = new RawFeature<>(true,
																						 true,
																						 () -> LMCoreDefinition.Features.GROUP.parameters);
	}
	static <T extends LMObject> Builder<T> builder() {return new GroupBuilder<>();}
	interface Builder<T extends LMObject> extends LMObject.Builder<Group<T>>
	{
		Builder<T> name(String name);
		Builder<T> concrete(boolean concrete);

		Builder<T> addInclude(Supplier<Reference<?>> include);
		Builder<T> addFeature(Supplier<Feature<?, ?>> feature);
		Builder<T> addGeneric(Supplier<Generic<?>> generic);
		Builder<T> addParameter(Supplier<Generic<?>> parameter);
	}
}
