package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.MetaModelBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

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

  interface RFeatures<T extends RFeatures<T>> extends Model.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, String> domain = Model.RFeatures.domain;
    RawFeature<String, List<String>> imports = Model.RFeatures.imports;
    RawFeature<String, List<String>> metamodels = Model.RFeatures.metamodels;
    RawFeature<Group<?>, List<Group<?>>> groups = new RawFeature<>(true,true,() -> MetaModel.Features.GROUPS);
    RawFeature<Enum<?>, List<Enum<?>>> enums = new RawFeature<>(true,true,() -> MetaModel.Features.ENUMS);
    RawFeature<Unit<?>, List<Unit<?>>> units = new RawFeature<>(true,true,() -> MetaModel.Features.UNITS);
    RawFeature<Alias, List<Alias>> aliases = new RawFeature<>(true,true,() -> MetaModel.Features.ALIASES);
    RawFeature<JavaWrapper<?>, List<JavaWrapper<?>>> javaWrappers = new RawFeature<>(true,true,() -> MetaModel.Features.JAVA_WRAPPERS);
    RawFeature<IModelPackage, IModelPackage> lmPackage = new RawFeature<>(false,false,() -> MetaModel.Features.LM_PACKAGE);
    RawFeature<Boolean, Boolean> genNamePackage = new RawFeature<>(false,false,() -> MetaModel.Features.GEN_NAME_PACKAGE);
    RawFeature<String, String> extraPackage = new RawFeature<>(false,false,() -> MetaModel.Features.EXTRA_PACKAGE);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int DOMAIN = Model.FeatureIDs.DOMAIN;
    int IMPORTS = Model.FeatureIDs.IMPORTS;
    int METAMODELS = Model.FeatureIDs.METAMODELS;
    int GROUPS = 1867707507;
    int ENUMS = -1604280045;
    int UNITS = -1589515024;
    int ALIASES = 856393791;
    int JAVA_WRAPPERS = -62373407;
    int LM_PACKAGE = 968992902;
    int GEN_NAME_PACKAGE = 1673060714;
    int EXTRA_PACKAGE = 768798069;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> DOMAIN = Model.Features.DOMAIN;
    Attribute<String, List<String>> IMPORTS = Model.Features.IMPORTS;
    Attribute<String, List<String>> METAMODELS = Model.Features.METAMODELS;
    Relation<Group<?>, List<Group<?>>> GROUPS = new RelationBuilder<Group<?>, List<Group<?>>>().name("groups").immutable(true).many(true).contains(true).rawFeature(MetaModel.RFeatures.groups).id(MetaModel.FeatureIDs.GROUPS).concept(() -> LMCoreModelDefinition.Groups.GROUP).build();
    Relation<Enum<?>, List<Enum<?>>> ENUMS = new RelationBuilder<Enum<?>, List<Enum<?>>>().name("enums").immutable(true).many(true).contains(true).rawFeature(MetaModel.RFeatures.enums).id(MetaModel.FeatureIDs.ENUMS).concept(() -> LMCoreModelDefinition.Groups.ENUM).build();
    Relation<Unit<?>, List<Unit<?>>> UNITS = new RelationBuilder<Unit<?>, List<Unit<?>>>().name("units").immutable(true).many(true).contains(true).rawFeature(MetaModel.RFeatures.units).id(MetaModel.FeatureIDs.UNITS).concept(() -> LMCoreModelDefinition.Groups.UNIT).build();
    Relation<Alias, List<Alias>> ALIASES = new RelationBuilder<Alias, List<Alias>>().name("aliases").immutable(true).many(true).contains(true).rawFeature(MetaModel.RFeatures.aliases).id(MetaModel.FeatureIDs.ALIASES).concept(() -> LMCoreModelDefinition.Groups.ALIAS).build();
    Relation<JavaWrapper<?>, List<JavaWrapper<?>>> JAVA_WRAPPERS = new RelationBuilder<JavaWrapper<?>, List<JavaWrapper<?>>>().name("javaWrappers").immutable(true).many(true).contains(true).rawFeature(MetaModel.RFeatures.javaWrappers).id(MetaModel.FeatureIDs.JAVA_WRAPPERS).concept(() -> LMCoreModelDefinition.Groups.JAVA_WRAPPER).build();
    Attribute<IModelPackage, IModelPackage> LM_PACKAGE = new AttributeBuilder<IModelPackage, IModelPackage>().name("lmPackage").immutable(true).mandatory(true).rawFeature(MetaModel.RFeatures.lmPackage).id(MetaModel.FeatureIDs.LM_PACKAGE).datatype(() -> LMCoreModelDefinition.JavaWrappers.I_MODEL_PACKAGE).build();
    Attribute<Boolean, Boolean> GEN_NAME_PACKAGE = new AttributeBuilder<Boolean, Boolean>().name("genNamePackage").immutable(true).defaultValue("true").rawFeature(MetaModel.RFeatures.genNamePackage).id(MetaModel.FeatureIDs.GEN_NAME_PACKAGE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<String, String> EXTRA_PACKAGE = new AttributeBuilder<String, String>().name("extraPackage").immutable(true).rawFeature(MetaModel.RFeatures.extraPackage).id(MetaModel.FeatureIDs.EXTRA_PACKAGE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, METAMODELS, GROUPS, ENUMS, UNITS, ALIASES, JAVA_WRAPPERS, LM_PACKAGE, GEN_NAME_PACKAGE, EXTRA_PACKAGE);
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
