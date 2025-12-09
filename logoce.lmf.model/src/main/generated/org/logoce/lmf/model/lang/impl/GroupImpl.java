package org.logoce.lmf.model.lang.impl;

import java.util.List;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Include;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Operation;

public final class GroupImpl<T extends LMObject> extends FeaturedObject implements Group<T> {
  private static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>().add(Group.Features.name, Group::name).add(Group.Features.concrete, Group::concrete).add(Group.Features.includes, Group::includes).add(Group.Features.features, Group::features).add(Group.Features.generics, Group::generics).add(Group.Features.operations, Group::operations).add(Group.Features.lmBuilder, Group::lmBuilder).build();
  private static final FeatureSetter<Group<?>> SET_MAP = new FeatureSetter.Builder<Group<?>>().build();
  private final String name;
  private final boolean concrete;
  private final List<Include<?>> includes;
  private final List<Feature<?, ?>> features;
  private final List<Generic<?>> generics;
  private final List<Operation> operations;
  private final BuilderSupplier<T> lmBuilder;

  public GroupImpl(final String name, final boolean concrete, final List<Include<?>> includes,
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
    eDeliver(true);
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
  public List<Include<?>> includes() {
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
    return LMCoreModelDefinition.Groups.GROUP;
  }

  @Override
  protected FeatureSetter<Group<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Group<?>> getterMap() {
    return GET_MAP;
  }

  @Override
  public int featureIndex(int featureId) {
    return switch (featureId) {
      case Group.FeatureIDs.NAME -> 0;
      case Group.FeatureIDs.CONCRETE -> 1;
      case Group.FeatureIDs.INCLUDES -> 2;
      case Group.FeatureIDs.FEATURES -> 3;
      case Group.FeatureIDs.GENERICS -> 4;
      case Group.FeatureIDs.OPERATIONS -> 5;
      case Group.FeatureIDs.LM_BUILDER -> 6;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
