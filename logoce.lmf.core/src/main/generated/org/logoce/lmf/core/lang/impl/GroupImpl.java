package org.logoce.lmf.core.lang.impl;

import java.util.List;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Generic;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Include;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Operation;

public final class GroupImpl<T extends LMObject> extends FeaturedObject<Group.Features<?>> implements Group<T> {
  private static final int FEATURE_COUNT = 7;
  private final ModelNotifier<Group.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final boolean concrete;
  private final List<Include<?>> includes;
  private final List<Feature<?, ?, ?, ?>> features;
  private final List<Generic<?>> generics;
  private final List<Operation> operations;
  private final BuilderSupplier<T> lmBuilder;

  public GroupImpl(final String name, final boolean concrete, final List<Include<?>> includes,
      final List<Feature<?, ?, ?, ?>> features, final List<Generic<?>> generics,
      final List<Operation> operations, final BuilderSupplier<T> lmBuilder) {
    this.name = name;
    this.concrete = concrete;
    this.includes = List.copyOf(includes);
    this.features = List.copyOf(features);
    this.generics = List.copyOf(generics);
    this.operations = List.copyOf(operations);
    this.lmBuilder = lmBuilder;
    setContainer(includes, Group.FeatureIDs.INCLUDES);
    setContainer(features, Group.FeatureIDs.FEATURES);
    setContainer(generics, Group.FeatureIDs.GENERICS);
    setContainer(operations, Group.FeatureIDs.OPERATIONS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Group.Features<?>> notifier() {
    return notifier;
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
  public List<Feature<?, ?, ?, ?>> features() {
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
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Group<?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
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

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Group<?>> GET_MAP = new FeatureGetter.Builder<Group<?>>(FEATURE_COUNT, GroupImpl::featureIndexStatic).add(Group.FeatureIDs.NAME, Group::name).add(Group.FeatureIDs.CONCRETE, Group::concrete).add(Group.FeatureIDs.INCLUDES, Group::includes).add(Group.FeatureIDs.FEATURES, Group::features).add(Group.FeatureIDs.GENERICS, Group::generics).add(Group.FeatureIDs.OPERATIONS, Group::operations).add(Group.FeatureIDs.LM_BUILDER, Group::lmBuilder).build();
    private static final FeatureSetter<Group<?>> SET_MAP = new FeatureSetter.Builder<Group<?>>(FEATURE_COUNT, GroupImpl::featureIndexStatic).build();
  }
}
