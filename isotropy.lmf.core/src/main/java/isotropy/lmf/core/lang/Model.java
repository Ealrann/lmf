package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.model.IModelPackage;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;
import java.util.function.Supplier;

public interface Model extends Named
{
	String domain();
	List<Group<?>> groups();
	List<Enum<?>> enums();
	List<Unit<?>> units();
	List<Alias> aliases();
	List<JavaWrapper<?>> javaWrappers();

	IModelPackage lPackage();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<String, String> domain = new RawFeature<>(false,
															 false,
															 () -> LMCoreDefinition.Features.MODEL.domain);
		RawFeature<Group<?>, List<Group<?>>> groups = new RawFeature<>(true,
																	   true,
																	   () -> LMCoreDefinition.Features.MODEL.groups);
		RawFeature<Enum<?>, List<Enum<?>>> enums = new RawFeature<>(true,
																	true,
																	() -> LMCoreDefinition.Features.MODEL.enums);
		RawFeature<Unit<?>, List<Unit<?>>> units = new RawFeature<>(true,
																	true,
																	() -> LMCoreDefinition.Features.MODEL.units);
		RawFeature<Alias, List<Alias>> aliases = new RawFeature<>(true,
																  true,
																  () -> LMCoreDefinition.Features.MODEL.aliases);
		RawFeature<JavaWrapper<?>, List<JavaWrapper<?>>> javaWrappers = new RawFeature<>(true,
																						 true,
																						 () -> LMCoreDefinition.Features.MODEL.javaWrappers);
	}

	static Builder builder() {return new ModelBuilder();}
	interface Builder extends LMObject.Builder<Model>
	{
		Builder name(String name);
		Builder domain(String domain);
		Builder lPackage(IModelPackage modelPackage);

		Builder addGroup(Supplier<? extends Group<?>> group);
		Builder addEnum(Supplier<? extends Enum<?>> _enum);
		Builder addUnit(Supplier<? extends Unit<?>> unit);
		Builder addAlias(Supplier<Alias> alias);
		Builder addJavaWrapper(Supplier<JavaWrapper<?>> javaWrapper);
	}
}
