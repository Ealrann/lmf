package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
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

  interface FeatureIDs<T extends FeatureIDs<T>> extends Model.FeatureIDs<T> {
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
