package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GroupBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.lang.impl.RelationImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Group<T extends LMObject> extends Type
{
	boolean concrete();
	List<? extends Group<?>> includes();
	List<? extends Feature<?, ?>> features();
	List<Generic> generics();

	Group<Group<?>> GROUP = LMCorePackage.GROUP_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", BoundType.Extends, LMObject.GROUP));

	interface Features
	{
		Attribute<String, String> Name = Named.Features.name;
		Attribute<Boolean, Boolean> Concrete = new AttributeImpl<>("concrete",
																   true,
																   false,
																   false,
																   LMCorePackage.BOOLEAN_UNIT);
		Relation<Group<?>, List<? extends Group<?>>> Includes = new RelationImpl<>("includes",
																				   true,
																				   true,
																				   false,
																				   Group.GROUP,
																				   false,
																				   null);
		Relation<Feature<?, ?>, List<? extends Feature<?, ?>>> Features = new RelationImpl<>("features",
																							 true,
																							 true,
																							 false,
																							 Feature.GROUP,
																							 true,
																							 null);
		Relation<Generic, List<Generic>> Generics = new RelationImpl<>("generics",
																	   true,
																	   true,
																	   false,
																	   Generic.GROUP,
																	   true,
																	   null);

		List<Feature<?, ?>> All = List.of(Name, Concrete, Includes, Features, Generics);
	}

	static <T extends LMObject> Builder<T> builder() { return new GroupBuilder<>(); }
	interface Builder<T extends LMObject> extends LMObject.Builder<Group<T>>
	{
		Builder<T> name(String name);
		Builder<T> concrete(boolean concrete);

		Builder<T> addInclude(Supplier<Group<?>> include);
		Builder<T> addFeature(Supplier<Feature<?, ?>> feature);
		Builder<T> addGeneric(Supplier<Generic> generic);
	}
}
