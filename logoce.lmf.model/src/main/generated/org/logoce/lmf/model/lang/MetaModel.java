package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.MetaModelBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.BooleanListener;
import org.logoce.lmf.model.notification.listener.Listener;

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

  interface Features<T extends Features<T>> extends Model.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, Model> DOMAIN = Model.Features.DOMAIN;
    Attribute<String, List<String>, Listener<List<String>>, Model> IMPORTS = Model.Features.IMPORTS;
    Attribute<String, List<String>, Listener<List<String>>, Model> METAMODELS = Model.Features.METAMODELS;
    Relation<Group<?>, List<Group<?>>, Listener<List<Group<?>>>, MetaModel> GROUPS = new RelationBuilder<Group<?>, List<Group<?>>, Listener<List<Group<?>>>, MetaModel>().name("groups").immutable(true).many(true).contains(true).id(MetaModel.FeatureIDs.GROUPS).concept(() -> LMCoreModelDefinition.Groups.GROUP).build();
    Relation<Enum<?>, List<Enum<?>>, Listener<List<Enum<?>>>, MetaModel> ENUMS = new RelationBuilder<Enum<?>, List<Enum<?>>, Listener<List<Enum<?>>>, MetaModel>().name("enums").immutable(true).many(true).contains(true).id(MetaModel.FeatureIDs.ENUMS).concept(() -> LMCoreModelDefinition.Groups.ENUM).build();
    Relation<Unit<?>, List<Unit<?>>, Listener<List<Unit<?>>>, MetaModel> UNITS = new RelationBuilder<Unit<?>, List<Unit<?>>, Listener<List<Unit<?>>>, MetaModel>().name("units").immutable(true).many(true).contains(true).id(MetaModel.FeatureIDs.UNITS).concept(() -> LMCoreModelDefinition.Groups.UNIT).build();
    Relation<Alias, List<Alias>, Listener<List<Alias>>, MetaModel> ALIASES = new RelationBuilder<Alias, List<Alias>, Listener<List<Alias>>, MetaModel>().name("aliases").immutable(true).many(true).contains(true).id(MetaModel.FeatureIDs.ALIASES).concept(() -> LMCoreModelDefinition.Groups.ALIAS).build();
    Relation<JavaWrapper<?>, List<JavaWrapper<?>>, Listener<List<JavaWrapper<?>>>, MetaModel> JAVA_WRAPPERS = new RelationBuilder<JavaWrapper<?>, List<JavaWrapper<?>>, Listener<List<JavaWrapper<?>>>, MetaModel>().name("javaWrappers").immutable(true).many(true).contains(true).id(MetaModel.FeatureIDs.JAVA_WRAPPERS).concept(() -> LMCoreModelDefinition.Groups.JAVA_WRAPPER).build();
    Attribute<IModelPackage, IModelPackage, Listener<IModelPackage>, MetaModel> LM_PACKAGE = new AttributeBuilder<IModelPackage, IModelPackage, Listener<IModelPackage>, MetaModel>().name("lmPackage").immutable(true).mandatory(true).id(MetaModel.FeatureIDs.LM_PACKAGE).datatype(() -> LMCoreModelDefinition.JavaWrappers.I_MODEL_PACKAGE).build();
    Attribute<Boolean, Boolean, BooleanListener, MetaModel> GEN_NAME_PACKAGE = new AttributeBuilder<Boolean, Boolean, BooleanListener, MetaModel>().name("genNamePackage").immutable(true).defaultValue("true").id(MetaModel.FeatureIDs.GEN_NAME_PACKAGE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<String, String, Listener<String>, MetaModel> EXTRA_PACKAGE = new AttributeBuilder<String, String, Listener<String>, MetaModel>().name("extraPackage").immutable(true).id(MetaModel.FeatureIDs.EXTRA_PACKAGE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, METAMODELS, GROUPS, ENUMS, UNITS, ALIASES, JAVA_WRAPPERS, LM_PACKAGE, GEN_NAME_PACKAGE, EXTRA_PACKAGE);
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
