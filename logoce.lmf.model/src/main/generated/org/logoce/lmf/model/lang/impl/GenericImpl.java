package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class GenericImpl<T> extends FeaturedObject implements Generic<T> {
  private static final FeatureGetter<Generic<?>> GET_MAP = new FeatureGetter.Builder<Generic<?>>().add(Generic.Features.name, Generic::name).add(Generic.Features.extension, Generic::extension).build();
  private static final FeatureSetter<Generic<?>> SET_MAP = new FeatureSetter.Builder<Generic<?>>().build();
  private final String name;
  private final GenericExtension extension;

  public GenericImpl(final String name, final GenericExtension extension) {
    this.name = name;
    this.extension = extension;
    setContainer(extension, Generic.Features.extension);
    eDeliver(true);
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
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Generic<?>> getterMap() {
    return GET_MAP;
  }

  @Override
  public int featureIndex(int featureId) {
    return switch (featureId) {
      case Generic.FeatureIDs.NAME -> 0;
      case Generic.FeatureIDs.EXTENSION -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
