package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GroupBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Group<T extends LMObject> extends Type<T>,Concept<T>
{
	boolean concrete();
	List<? extends Reference<?>> includes();
	List<? extends Feature<?, ?>> features();
	List<? extends Generic<?>> generics();
	List<? extends Generic<?>> parameters();

	List<Generic<?>> GENERICS = List.of(new GenericImpl<>("T", BoundType.Extends, LMCoreDefinition.Groups.LM_OBJECT));

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
