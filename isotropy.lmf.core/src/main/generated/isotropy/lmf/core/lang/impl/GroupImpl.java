package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Reference;
import java.lang.Override;
import java.lang.String;
import java.util.List;

public final class GroupImpl<T extends LMObject> extends FeaturedObject implements Group<T> {
  private static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>().add(isotropy.lmf.core.lang.Group.Features.name, isotropy.lmf.core.lang.Group::name).add(isotropy.lmf.core.lang.Group.Features.concrete, isotropy.lmf.core.lang.Group::concrete).add(isotropy.lmf.core.lang.Group.Features.includes, isotropy.lmf.core.lang.Group::includes).add(isotropy.lmf.core.lang.Group.Features.features, isotropy.lmf.core.lang.Group::features).add(isotropy.lmf.core.lang.Group.Features.generics, isotropy.lmf.core.lang.Group::generics).build();

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
