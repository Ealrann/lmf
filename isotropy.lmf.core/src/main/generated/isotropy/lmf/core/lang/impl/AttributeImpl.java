package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Datatype;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import java.lang.Override;
import java.lang.String;
import java.util.List;

public final class AttributeImpl<UnaryType, EffectiveType> extends FeaturedObject implements Attribute<UnaryType, EffectiveType> {
  private static final FeatureGetter<Attribute<?, ?>> GET_MAP = new FeatureGetter.Builder<Attribute<?, ?>>().add(isotropy.lmf.core.lang.Attribute.Features.name, isotropy.lmf.core.lang.Attribute::name).add(isotropy.lmf.core.lang.Attribute.Features.immutable, isotropy.lmf.core.lang.Attribute::immutable).add(isotropy.lmf.core.lang.Attribute.Features.many, isotropy.lmf.core.lang.Attribute::many).add(isotropy.lmf.core.lang.Attribute.Features.mandatory, isotropy.lmf.core.lang.Attribute::mandatory).add(isotropy.lmf.core.lang.Attribute.Features.rawFeature, isotropy.lmf.core.lang.Attribute::rawFeature).add(isotropy.lmf.core.lang.Attribute.Features.datatype, isotropy.lmf.core.lang.Attribute::datatype).add(isotropy.lmf.core.lang.Attribute.Features.defaultValue, isotropy.lmf.core.lang.Attribute::defaultValue).add(isotropy.lmf.core.lang.Attribute.Features.parameters, isotropy.lmf.core.lang.Attribute::parameters).build();

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
