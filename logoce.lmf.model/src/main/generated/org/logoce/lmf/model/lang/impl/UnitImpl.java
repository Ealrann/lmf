package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Unit;

public final class UnitImpl<T> extends FeaturedObject implements Unit<T> {
  private static final FeatureGetter<Unit<?>> GET_MAP = new FeatureGetter.Builder<Unit<?>>().add(org.logoce.lmf.model.lang.Unit.Features.name, org.logoce.lmf.model.lang.Unit::name).add(org.logoce.lmf.model.lang.Unit.Features.matcher, org.logoce.lmf.model.lang.Unit::matcher).add(org.logoce.lmf.model.lang.Unit.Features.defaultValue, org.logoce.lmf.model.lang.Unit::defaultValue).add(org.logoce.lmf.model.lang.Unit.Features.primitive, org.logoce.lmf.model.lang.Unit::primitive).add(org.logoce.lmf.model.lang.Unit.Features.extractor, org.logoce.lmf.model.lang.Unit::extractor).build();
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
    return LMCoreDefinition.Groups.UNIT;
  }

  @Override
  protected FeatureSetter<Unit<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Unit<?>> getterMap() {
    return GET_MAP;
  }
}
