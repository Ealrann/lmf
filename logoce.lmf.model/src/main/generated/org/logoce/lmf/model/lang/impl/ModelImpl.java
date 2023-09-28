package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Unit;

public final class ModelImpl extends FeaturedObject implements Model {
  private static final FeatureGetter<Model> GET_MAP = new FeatureGetter.Builder<Model>().add(org.logoce.lmf.model.lang.Model.Features.name, org.logoce.lmf.model.lang.Model::name).add(org.logoce.lmf.model.lang.Model.Features.domain, org.logoce.lmf.model.lang.Model::domain).add(org.logoce.lmf.model.lang.Model.Features.groups, org.logoce.lmf.model.lang.Model::groups).add(org.logoce.lmf.model.lang.Model.Features.enums, org.logoce.lmf.model.lang.Model::enums).add(org.logoce.lmf.model.lang.Model.Features.units, org.logoce.lmf.model.lang.Model::units).add(org.logoce.lmf.model.lang.Model.Features.aliases, org.logoce.lmf.model.lang.Model::aliases).add(org.logoce.lmf.model.lang.Model.Features.javaWrappers, org.logoce.lmf.model.lang.Model::javaWrappers).add(org.logoce.lmf.model.lang.Model.Features.lPackage, org.logoce.lmf.model.lang.Model::lPackage).build();

  private static final FeatureSetter<Model> SET_MAP = new FeatureSetter.Builder<Model>().build();

  private final String name;

  private final String domain;

  private final List<Group<?>> groups;

  private final List<Enum<?>> enums;

  private final List<Unit<?>> units;

  private final List<Alias> aliases;

  private final List<JavaWrapper<?>> javaWrappers;

  private final IModelPackage lPackage;

  public ModelImpl(final String name, final String domain, final List<Group<?>> groups,
      final List<Enum<?>> enums, final List<Unit<?>> units, final List<Alias> aliases,
      final List<JavaWrapper<?>> javaWrappers, final IModelPackage lPackage) {
    this.name = name;
    this.domain = domain;
    this.groups = List.copyOf(groups);
    this.enums = List.copyOf(enums);
    this.units = List.copyOf(units);
    this.aliases = List.copyOf(aliases);
    this.javaWrappers = List.copyOf(javaWrappers);
    this.lPackage = lPackage;
    setContainer(groups, Features.groups);
    setContainer(enums, Features.enums);
    setContainer(units, Features.units);
    setContainer(aliases, Features.aliases);
    setContainer(javaWrappers, Features.javaWrappers);
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
  public IModelPackage lPackage() {
    return lPackage;
  }

  @Override
  public Group<Model> lmGroup() {
    return LMCoreDefinition.Groups.MODEL;
  }

  @Override
  protected FeatureSetter<Model> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Model> getterMap() {
    return GET_MAP;
  }
}
