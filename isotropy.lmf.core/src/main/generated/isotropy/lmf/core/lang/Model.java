package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.ModelBuilder;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.IModelPackage;
import isotropy.lmf.core.model.RawFeature;
import java.lang.String;
import java.util.List;
import java.util.function.Supplier;

public interface Model extends Named {
  static Builder builder() {
    return new ModelBuilder();
  }

  String domain();

  List<Group<?>> groups();

  List<Enum<?>> enums();

  List<Unit<?>> units();

  List<Alias> aliases();

  List<JavaWrapper<?>> javaWrappers();

  IModelPackage lPackage();

  interface Features {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, String> domain = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.MODEL.DOMAIN);

    RawFeature<Group<?>, List<Group<?>>> groups = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.MODEL.GROUPS);

    RawFeature<Enum<?>, List<Enum<?>>> enums = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.MODEL.ENUMS);

    RawFeature<Unit<?>, List<Unit<?>>> units = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.MODEL.UNITS);

    RawFeature<Alias, List<Alias>> aliases = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.MODEL.ALIASES);

    RawFeature<JavaWrapper<?>, List<JavaWrapper<?>>> javaWrappers = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.MODEL.JAVA_WRAPPERS);

    RawFeature<IModelPackage, IModelPackage> lPackage = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.MODEL.L_PACKAGE);
  }

  interface Builder extends IFeaturedObject.Builder<Model> {
    Builder name(String name);

    Builder domain(String domain);

    Builder addGroup(Supplier<Group<?>> group);

    Builder addEnum(Supplier<Enum<?>> _enum);

    Builder addUnit(Supplier<Unit<?>> unit);

    Builder addAliase(Supplier<Alias> aliase);

    Builder addJavaWrapper(Supplier<JavaWrapper<?>> javaWrapper);

    Builder lPackage(IModelPackage lPackage);
  }
}
