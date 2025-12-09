package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Unit;

public final class UnitImpl<T> extends FeaturedObject implements Unit<T> {
  private static final FeatureGetter<Unit<?>> GET_MAP = new FeatureGetter.Builder<Unit<?>>().add(Unit.Features.name, Unit::name).add(Unit.Features.matcher, Unit::matcher).add(Unit.Features.defaultValue, Unit::defaultValue).add(Unit.Features.primitive, Unit::primitive).add(Unit.Features.extractor, Unit::extractor).build();
  private static final FeatureSetter<Unit<?>> SET_MAP = new FeatureSetter.Builder<Unit<?>>().build();
  private final String name;
  private final String matcher;
  private final String defaultValue;
  private final Primitive primitive;
  private final String extractor;

  public UnitImpl(final String name, final String matcher, final String defaultValue,
      final Primitive primitive, final String extractor) {
    this.name = name;
    this.matcher = matcher;
    this.defaultValue = defaultValue;
    this.primitive = primitive;
    this.extractor = extractor;
    eDeliver(true);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String matcher() {
    return matcher;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public Primitive primitive() {
    return primitive;
  }

  @Override
  public String extractor() {
    return extractor;
  }

  @Override
  public Group<Unit<?>> lmGroup() {
    return LMCoreModelDefinition.Groups.UNIT;
  }

  @Override
  protected FeatureSetter<Unit<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Unit<?>> getterMap() {
    return GET_MAP;
  }

  @Override
  public int featureIndex(int featureId) {
    return switch (featureId) {
      case Unit.FeatureIDs.NAME -> 0;
      case Unit.FeatureIDs.MATCHER -> 1;
      case Unit.FeatureIDs.DEFAULT_VALUE -> 2;
      case Unit.FeatureIDs.PRIMITIVE -> 3;
      case Unit.FeatureIDs.EXTRACTOR -> 4;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
