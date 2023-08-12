package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.lang.impl.RelationImpl;
import isotropy.lmf.core.model.IModelPackage;

import java.util.List;
import java.util.function.Supplier;

public interface Model extends Named
{
	List<Group<?>> groups();
	List<Enum<?>> enums();
	List<Unit<?>> units();
	List<Alias> aliases();

	IModelPackage lPackage();

	Group<Model> GROUP = LMCorePackage.MODEL_GROUP;

	interface Features
	{
		Attribute<String, String> name = Named.Features.name;
		Relation<Group<?>, List<Group<?>>> groups = new RelationImpl<>("groups",
																	   true,
																	   true,
																	   false,
																	   Group.GROUP,
																	   true,
																	   null);
		Relation<Enum<?>, List<Enum<?>>> enums = new RelationImpl<>("enums", true, true, false, Enum.GROUP, true, null);
		Relation<Unit<?>, List<Unit<?>>> units = new RelationImpl<>("units", true, true, false, Unit.GROUP, true, null);
		Relation<Alias, List<Alias>> aliases = new RelationImpl<>("aliases",
																  true,
																  true,
																  false,
																  Alias.GROUP,
																  true,
																  null);

		List<Feature<?, ?>> All = List.of(name, groups, enums, units, aliases);
	}

	static Builder builder() { return new ModelBuilder();}
	interface Builder extends LMObject.Builder<Model>
	{
		Builder name(String name);
		Builder lPackage(IModelPackage modelPackage);

		Builder addGroup(Supplier<? extends Group<?>> group);
		Builder addEnum(Supplier<? extends Enum<?>> _enum);
		Builder addUnit(Supplier<? extends Unit<?>> unit);
		Builder addAlias(Supplier<Alias> alias);
	}
}
