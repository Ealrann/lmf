package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.builder.MetaModelBuilder;

public interface MetaModel extends Model {
  static Builder builder() {
    return new MetaModelBuilder();
  }

  List<Group<?>> groups();
  List<Enum<?>> enums();
  List<Unit<?>> units();
  List<Alias> aliases();
  List<JavaWrapper<?>> javaWrappers();
  IModelPackage lmPackage();
  boolean genNamePackage();
  String extraPackage();

  interface Features<T extends Features<T>> extends Model.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> domain = Model.Features.domain;
    RawFeature<String, List<String>> imports = Model.Features.imports;
    RawFeature<String, List<String>> metamodels = Model.Features.metamodels;
    RawFeature<Group<?>, List<Group<?>>> groups = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.META_MODEL.GROUPS);
    RawFeature<Enum<?>, List<Enum<?>>> enums = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.META_MODEL.ENUMS);
    RawFeature<Unit<?>, List<Unit<?>>> units = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.META_MODEL.UNITS);
    RawFeature<Alias, List<Alias>> aliases = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.META_MODEL.ALIASES);
    RawFeature<JavaWrapper<?>, List<JavaWrapper<?>>> javaWrappers = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.META_MODEL.JAVA_WRAPPERS);
    RawFeature<IModelPackage, IModelPackage> lmPackage = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.META_MODEL.LM_PACKAGE);
    RawFeature<Boolean, Boolean> genNamePackage = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.META_MODEL.GEN_NAME_PACKAGE);
    RawFeature<String, String> extraPackage = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.META_MODEL.EXTRA_PACKAGE);
  }

  interface Builder extends IFeaturedObject.Builder<MetaModel> {
    Builder name(String name);
    Builder domain(String domain);
    Builder addImport(String _import);
    Builder addMetamodel(String metamodel);
    Builder addGroup(Supplier<Group<?>> group);
    Builder addEnum(Supplier<Enum<?>> _enum);
    Builder addUnit(Supplier<Unit<?>> unit);
    Builder addAliase(Supplier<Alias> aliase);
    Builder addJavaWrapper(Supplier<JavaWrapper<?>> javaWrapper);
    Builder lmPackage(IModelPackage lmPackage);
    Builder genNamePackage(boolean genNamePackage);
    Builder extraPackage(String extraPackage);
    Builder addGroups(List<Group<?>> groups);
    Builder addEnums(List<Enum<?>> enums);
    Builder addUnits(List<Unit<?>> units);
    Builder addAliases(List<Alias> aliases);
    Builder addJavaWrappers(List<JavaWrapper<?>> javaWrappers);
    Builder addImports(List<String> imports);
    Builder addMetamodels(List<String> metamodels);
  }
}
