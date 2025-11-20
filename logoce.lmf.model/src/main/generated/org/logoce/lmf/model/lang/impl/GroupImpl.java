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
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.Reference;

public final class GroupImpl<T extends LMObject> extends FeaturedObject implements Group<T> {
  private static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>().add(org.logoce.lmf.model.lang.Group.Features.name, org.logoce.lmf.model.lang.Group::name).add(org.logoce.lmf.model.lang.Group.Features.concrete, org.logoce.lmf.model.lang.Group::concrete).add(org.logoce.lmf.model.lang.Group.Features.includes, org.logoce.lmf.model.lang.Group::includes).add(org.logoce.lmf.model.lang.Group.Features.features, org.logoce.lmf.model.lang.Group::features).add(org.logoce.lmf.model.lang.Group.Features.generics, org.logoce.lmf.model.lang.Group::generics).add(org.logoce.lmf.model.lang.Group.Features.operations, org.logoce.lmf.model.lang.Group::operations).add(org.logoce.lmf.model.lang.Group.Features.lmBuilder, org.logoce.lmf.model.lang.Group::lmBuilder).build();
  private static final FeatureSetter<Group<?>> SET_MAP = new FeatureSetter.Builder<Group<?>>().build();
  private final String name;
  private final boolean concrete;
  private final List<Reference<?>> includes;
  private final List<Feature<?, ?>> features;
  private final List<Generic<?>> generics;
  private final List<Operation> operations;
  private final BuilderSupplier<T> lmBuilder;

  public GroupImpl(final String name, final boolean concrete, final List<Reference<?>> includes,
      final List<Feature<?, ?>> features, final List<Generic<?>> generics,
      final List<Operation> operations, final BuilderSupplier<T> lmBuilder) {
    this.name = name;
    this.concrete = concrete;
    this.includes = List.copyOf(includes);
    this.features = List.copyOf(features);
    this.generics = List.copyOf(generics);
    this.operations = List.copyOf(operations);
    this.lmBuilder = lmBuilder;
    setContainer(includes, Group.Features.includes);
    setContainer(features, Group.Features.features);
    setContainer(generics, Group.Features.generics);
    setContainer(operations, Group.Features.operations);
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
  public List<Operation> operations() {
    return operations;
  }

  @Override
  public BuilderSupplier<T> lmBuilder() {
    return lmBuilder;
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
