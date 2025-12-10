package org.logoce.lmf.model.lang.impl;

import java.util.List;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Unit;

public final class MetaModelImpl extends FeaturedObject implements MetaModel {
  private final String name;
  private final String domain;
  private final List<String> imports;
  private final List<String> metamodels;
  private final List<Group<?>> groups;
  private final List<Enum<?>> enums;
  private final List<Unit<?>> units;
  private final List<Alias> aliases;
  private final List<JavaWrapper<?>> javaWrappers;
  private final IModelPackage lmPackage;
  private final boolean genNamePackage;
  private final String extraPackage;

  public MetaModelImpl(final String name, final String domain, final List<String> imports,
      final List<String> metamodels, final List<Group<?>> groups, final List<Enum<?>> enums,
      final List<Unit<?>> units, final List<Alias> aliases, final List<JavaWrapper<?>> javaWrappers,
      final IModelPackage lmPackage, final boolean genNamePackage, final String extraPackage) {
    this.name = name;
    this.domain = domain;
    this.imports = List.copyOf(imports);
    this.metamodels = List.copyOf(metamodels);
    this.groups = List.copyOf(groups);
    this.enums = List.copyOf(enums);
    this.units = List.copyOf(units);
    this.aliases = List.copyOf(aliases);
    this.javaWrappers = List.copyOf(javaWrappers);
    this.lmPackage = lmPackage;
    this.genNamePackage = genNamePackage;
    this.extraPackage = extraPackage;
    setContainer(groups, MetaModel.FeatureIDs.GROUPS);
    setContainer(enums, MetaModel.FeatureIDs.ENUMS);
    setContainer(units, MetaModel.FeatureIDs.UNITS);
    setContainer(aliases, MetaModel.FeatureIDs.ALIASES);
    setContainer(javaWrappers, MetaModel.FeatureIDs.JAVA_WRAPPERS);
    eDeliver(true);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String domain() {
    return domain;
  }

  @Override
  public List<String> imports() {
    return imports;
  }

  @Override
  public List<String> metamodels() {
    return metamodels;
  }

  @Override
  public List<Group<?>> groups() {
    return groups;
  }

  @Override
  public List<Enum<?>> enums() {
    return enums;
  }

  @Override
  public List<Unit<?>> units() {
    return units;
  }

  @Override
  public List<Alias> aliases() {
    return aliases;
  }

  @Override
  public List<JavaWrapper<?>> javaWrappers() {
    return javaWrappers;
  }

  @Override
  public IModelPackage lmPackage() {
    return lmPackage;
  }

  @Override
  public boolean genNamePackage() {
    return genNamePackage;
  }

  @Override
  public String extraPackage() {
    return extraPackage;
  }

  @Override
  public Group<MetaModel> lmGroup() {
    return LMCoreModelDefinition.Groups.META_MODEL;
  }

  @Override
  protected FeatureSetter<MetaModel> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<MetaModel> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case MetaModel.FeatureIDs.NAME -> 0;
      case MetaModel.FeatureIDs.DOMAIN -> 1;
      case MetaModel.FeatureIDs.IMPORTS -> 2;
      case MetaModel.FeatureIDs.METAMODELS -> 3;
      case MetaModel.FeatureIDs.GROUPS -> 4;
      case MetaModel.FeatureIDs.ENUMS -> 5;
      case MetaModel.FeatureIDs.UNITS -> 6;
      case MetaModel.FeatureIDs.ALIASES -> 7;
      case MetaModel.FeatureIDs.JAVA_WRAPPERS -> 8;
      case MetaModel.FeatureIDs.LM_PACKAGE -> 9;
      case MetaModel.FeatureIDs.GEN_NAME_PACKAGE -> 10;
      case MetaModel.FeatureIDs.EXTRA_PACKAGE -> 11;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<MetaModel> GET_MAP = new FeatureGetter.Builder<MetaModel>(12, MetaModelImpl::featureIndexStatic).add(MetaModel.FeatureIDs.NAME, MetaModel::name).add(MetaModel.FeatureIDs.DOMAIN, MetaModel::domain).add(MetaModel.FeatureIDs.IMPORTS, MetaModel::imports).add(MetaModel.FeatureIDs.METAMODELS, MetaModel::metamodels).add(MetaModel.FeatureIDs.GROUPS, MetaModel::groups).add(MetaModel.FeatureIDs.ENUMS, MetaModel::enums).add(MetaModel.FeatureIDs.UNITS, MetaModel::units).add(MetaModel.FeatureIDs.ALIASES, MetaModel::aliases).add(MetaModel.FeatureIDs.JAVA_WRAPPERS, MetaModel::javaWrappers).add(MetaModel.FeatureIDs.LM_PACKAGE, MetaModel::lmPackage).add(MetaModel.FeatureIDs.GEN_NAME_PACKAGE, MetaModel::genNamePackage).add(MetaModel.FeatureIDs.EXTRA_PACKAGE, MetaModel::extraPackage).build();
    private static final FeatureSetter<MetaModel> SET_MAP = new FeatureSetter.Builder<MetaModel>(12, MetaModelImpl::featureIndexStatic).build();
  }
}
