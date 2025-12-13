package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.ModelNotifier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class GenericImpl<T> extends FeaturedObject<Generic.Features<?>> implements Generic<T> {
  private static final int FEATURE_COUNT = 2;
  private final ModelNotifier<Generic.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final GenericExtension extension;

  public GenericImpl(final String name, final GenericExtension extension) {
    this.name = name;
    this.extension = extension;
    setContainer(extension, Generic.FeatureIDs.EXTENSION);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Generic.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public GenericExtension extension() {
    return extension;
  }

  @Override
  public Group<Generic<?>> lmGroup() {
    return LMCoreModelDefinition.Groups.GENERIC;
  }

  @Override
  protected FeatureSetter<Generic<?>> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Generic<?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Generic.FeatureIDs.NAME -> 0;
      case Generic.FeatureIDs.EXTENSION -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Generic<?>> GET_MAP = new FeatureGetter.Builder<Generic<?>>(FEATURE_COUNT, GenericImpl::featureIndexStatic).add(Generic.FeatureIDs.NAME, Generic::name).add(Generic.FeatureIDs.EXTENSION, Generic::extension).build();
    private static final FeatureSetter<Generic<?>> SET_MAP = new FeatureSetter.Builder<Generic<?>>(FEATURE_COUNT, GenericImpl::featureIndexStatic).build();
  }
}
