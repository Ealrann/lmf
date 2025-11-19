package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Reference;
import org.logoce.lmf.model.notification.impl.SetNotifiation;

public final class GroupImpl<T extends LMObject> extends FeaturedObject implements Group<T> {
  private static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>().add(org.logoce.lmf.model.lang.Group.Features.name, org.logoce.lmf.model.lang.Group::name).add(org.logoce.lmf.model.lang.Group.Features.concrete, org.logoce.lmf.model.lang.Group::concrete).add(org.logoce.lmf.model.lang.Group.Features.includes, org.logoce.lmf.model.lang.Group::includes).add(org.logoce.lmf.model.lang.Group.Features.features, org.logoce.lmf.model.lang.Group::features).add(org.logoce.lmf.model.lang.Group.Features.generics, org.logoce.lmf.model.lang.Group::generics).add(org.logoce.lmf.model.lang.Group.Features.lmBuilder, org.logoce.lmf.model.lang.Group::lmBuilder).build();
  private static final FeatureSetter<Group<?>> SET_MAP = new FeatureSetter.Builder<Group<?>>().add(org.logoce.lmf.model.lang.Group.Features.lmBuilder, (object, value) -> ((org.logoce.lmf.model.lang.impl.GroupImpl) object).lmBuilder(value)).build();
  private final String name;
  private final boolean concrete;
  private final List<Reference<?>> includes;
  private final List<Feature<?, ?>> features;
  private final List<Generic<?>> generics;
  private BuilderSupplier<T> lmBuilder;

  public GroupImpl(final String name, final boolean concrete, final List<Reference<?>> includes,
      final List<Feature<?, ?>> features, final List<Generic<?>> generics) {
    this.name = name;
    this.concrete = concrete;
    this.includes = List.copyOf(includes);
    this.features = List.copyOf(features);
    this.generics = List.copyOf(generics);
    setContainer(includes, Group.Features.includes);
    setContainer(features, Group.Features.features);
    setContainer(generics, Group.Features.generics);
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
  public BuilderSupplier<T> lmBuilder() {
    return lmBuilder;
  }

  @Override
  public void lmBuilder(final BuilderSupplier<T> lmBuilder) {
    final var oldValue = this.lmBuilder;
    this.lmBuilder = lmBuilder;
    eNotify(new SetNotifiation(this, Group.Features.lmBuilder, lmBuilder, oldValue));
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
