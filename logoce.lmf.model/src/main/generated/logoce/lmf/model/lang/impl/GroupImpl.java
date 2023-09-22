package logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import logoce.lmf.model.api.model.FeaturedObject;
import logoce.lmf.model.feature.FeatureGetter;
import logoce.lmf.model.feature.FeatureSetter;
import logoce.lmf.model.lang.Feature;
import logoce.lmf.model.lang.Generic;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.LMCoreDefinition;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.lang.Reference;

public final class GroupImpl<T extends LMObject> extends FeaturedObject implements Group<T> {
  private static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>().add(logoce.lmf.model.lang.Group.Features.name, logoce.lmf.model.lang.Group::name).add(logoce.lmf.model.lang.Group.Features.concrete, logoce.lmf.model.lang.Group::concrete).add(logoce.lmf.model.lang.Group.Features.includes, logoce.lmf.model.lang.Group::includes).add(logoce.lmf.model.lang.Group.Features.features, logoce.lmf.model.lang.Group::features).add(logoce.lmf.model.lang.Group.Features.generics, logoce.lmf.model.lang.Group::generics).build();

  private static final FeatureSetter<Group<?>> SET_MAP = new FeatureSetter.Builder<Group<?>>().build();

  private final String name;

  private final boolean concrete;

  private final List<Reference<?>> includes;

  private final List<Feature<?, ?>> features;

  private final List<Generic<?>> generics;

  public GroupImpl(final String name, final boolean concrete, final List<Reference<?>> includes,
      final List<Feature<?, ?>> features, final List<Generic<?>> generics) {
    this.name = name;
    this.concrete = concrete;
    this.includes = List.copyOf(includes);
    this.features = List.copyOf(features);
    this.generics = List.copyOf(generics);
    setContainer(includes, Features.includes);
    setContainer(features, Features.features);
    setContainer(generics, Features.generics);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean concrete() {
    return concrete;
  }

  @Override
  public List<Reference<?>> includes() {
    return includes;
  }

  @Override
  public List<Feature<?, ?>> features() {
    return features;
  }

  @Override
  public List<Generic<?>> generics() {
    return generics;
  }

  @Override
  public Group<Group<?>> lmGroup() {
    return LMCoreDefinition.Groups.GROUP;
  }

  @Override
  protected FeatureSetter<Group<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Group<?>> getterMap() {
    return GET_MAP;
  }
}
