package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Datatype;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;

public final class AttributeImpl<UnaryType, EffectiveType> extends FeaturedObject implements Attribute<UnaryType, EffectiveType> {
  private static final FeatureGetter<Attribute<?, ?>> GET_MAP = new FeatureGetter.Builder<Attribute<?, ?>>().add(org.logoce.lmf.model.lang.Attribute.Features.name, org.logoce.lmf.model.lang.Attribute::name).add(org.logoce.lmf.model.lang.Attribute.Features.immutable, org.logoce.lmf.model.lang.Attribute::immutable).add(org.logoce.lmf.model.lang.Attribute.Features.many, org.logoce.lmf.model.lang.Attribute::many).add(org.logoce.lmf.model.lang.Attribute.Features.mandatory, org.logoce.lmf.model.lang.Attribute::mandatory).add(org.logoce.lmf.model.lang.Attribute.Features.rawFeature, org.logoce.lmf.model.lang.Attribute::rawFeature).add(org.logoce.lmf.model.lang.Attribute.Features.datatype, org.logoce.lmf.model.lang.Attribute::datatype).add(org.logoce.lmf.model.lang.Attribute.Features.defaultValue, org.logoce.lmf.model.lang.Attribute::defaultValue).add(org.logoce.lmf.model.lang.Attribute.Features.parameters, org.logoce.lmf.model.lang.Attribute::parameters).build();

  private static final FeatureSetter<Attribute<?, ?>> SET_MAP = new FeatureSetter.Builder<Attribute<?, ?>>().build();

  private final String name;

  private final boolean immutable;

  private final boolean many;

  private final boolean mandatory;

  private final RawFeature<UnaryType, EffectiveType> rawFeature;

  private final Datatype<UnaryType> datatype;

  private final String defaultValue;

  private final List<Generic<?>> parameters;

  public AttributeImpl(final String name, final boolean immutable, final boolean many,
      final boolean mandatory, final RawFeature<UnaryType, EffectiveType> rawFeature,
      final Datatype<UnaryType> datatype, final String defaultValue,
      final List<Generic<?>> parameters) {
    this.name = name;
    this.immutable = immutable;
    this.many = many;
    this.mandatory = mandatory;
    this.rawFeature = rawFeature;
    this.datatype = datatype;
    this.defaultValue = defaultValue;
    this.parameters = List.copyOf(parameters);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean immutable() {
    return immutable;
  }

  @Override
  public boolean many() {
    return many;
  }

  @Override
  public boolean mandatory() {
    return mandatory;
  }

  @Override
  public RawFeature<UnaryType, EffectiveType> rawFeature() {
    return rawFeature;
  }

  @Override
  public Datatype<UnaryType> datatype() {
    return datatype;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public List<Generic<?>> parameters() {
    return parameters;
  }

  @Override
  public Group<Attribute<?, ?>> lmGroup() {
    return LMCoreDefinition.Groups.ATTRIBUTE;
  }

  @Override
  protected FeatureSetter<Attribute<?, ?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Attribute<?, ?>> getterMap() {
    return GET_MAP;
  }
}
