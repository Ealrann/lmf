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
  private static final FeatureGetter<MetaModel> GET_MAP = new FeatureGetter.Builder<MetaModel>().add(MetaModel.Features.name, MetaModel::name).add(MetaModel.Features.domain, MetaModel::domain).add(MetaModel.Features.imports, MetaModel::imports).add(MetaModel.Features.metamodels, MetaModel::metamodels).add(MetaModel.Features.groups, MetaModel::groups).add(MetaModel.Features.enums, MetaModel::enums).add(MetaModel.Features.units, MetaModel::units).add(MetaModel.Features.aliases, MetaModel::aliases).add(MetaModel.Features.javaWrappers, MetaModel::javaWrappers).add(MetaModel.Features.lmPackage, MetaModel::lmPackage).add(MetaModel.Features.genNamePackage, MetaModel::genNamePackage).add(MetaModel.Features.extraPackage, MetaModel::extraPackage).build();
  private static final FeatureSetter<MetaModel> SET_MAP = new FeatureSetter.Builder<MetaModel>().build();
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
    setContainer(groups, MetaModel.Features.groups);
    setContainer(enums, MetaModel.Features.enums);
    setContainer(units, MetaModel.Features.units);
    setContainer(aliases, MetaModel.Features.aliases);
    setContainer(javaWrappers, MetaModel.Features.javaWrappers);
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
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<MetaModel> getterMap() {
    return GET_MAP;
  }
}
