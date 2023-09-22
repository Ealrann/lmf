package logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import logoce.lmf.model.api.model.FeaturedObject;
import logoce.lmf.model.api.model.IModelPackage;
import logoce.lmf.model.feature.FeatureGetter;
import logoce.lmf.model.feature.FeatureSetter;
import logoce.lmf.model.lang.Alias;
import logoce.lmf.model.lang.Enum;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.JavaWrapper;
import logoce.lmf.model.lang.LMCoreDefinition;
import logoce.lmf.model.lang.Model;
import logoce.lmf.model.lang.Unit;

public final class ModelImpl extends FeaturedObject implements Model {
  private static final FeatureGetter<Model> GET_MAP = new FeatureGetter.Builder<Model>().add(logoce.lmf.model.lang.Model.Features.name, logoce.lmf.model.lang.Model::name).add(logoce.lmf.model.lang.Model.Features.domain, logoce.lmf.model.lang.Model::domain).add(logoce.lmf.model.lang.Model.Features.groups, logoce.lmf.model.lang.Model::groups).add(logoce.lmf.model.lang.Model.Features.enums, logoce.lmf.model.lang.Model::enums).add(logoce.lmf.model.lang.Model.Features.units, logoce.lmf.model.lang.Model::units).add(logoce.lmf.model.lang.Model.Features.aliases, logoce.lmf.model.lang.Model::aliases).add(logoce.lmf.model.lang.Model.Features.javaWrappers, logoce.lmf.model.lang.Model::javaWrappers).add(logoce.lmf.model.lang.Model.Features.lPackage, logoce.lmf.model.lang.Model::lPackage).build();

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
