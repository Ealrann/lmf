package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GroupBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Group<T extends LMObject> extends Type
{
	boolean concrete();
	List<? extends Group<?>> includes();
	List<? extends Feature<?, ?>> features();
	List<Generic> generics();

	List<Generic> GENERICS = List.of(new GenericImpl("T", BoundType.Extends, LMCoreDefinition.Groups.LM_OBJECT));

	static <T extends LMObject> Builder<T> builder() {return new GroupBuilder<>();}
	interface Builder<T extends LMObject> extends LMObject.Builder<Group<T>>
	{
		Builder<T> name(String name);
		Builder<T> concrete(boolean concrete);

		Builder<T> addInclude(Supplier<Group<?>> include);
		Builder<T> addFeature(Supplier<Feature<?, ?>> feature);
		Builder<T> addGeneric(Supplier<Generic> generic);
	}
}
