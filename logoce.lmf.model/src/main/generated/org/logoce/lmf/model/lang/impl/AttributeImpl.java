package org.logoce.lmf.model.lang.impl;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Datatype;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class AttributeImpl<UnaryType, EffectiveType> extends FeaturedObject implements Attribute<UnaryType, EffectiveType> {
  private static final FeatureGetter<Attribute<?, ?>> GET_MAP = new FeatureGetter.Builder<Attribute<?, ?>>().add(Attribute.RFeatures.name, Attribute::name).add(Attribute.RFeatures.immutable, Attribute::immutable).add(Attribute.RFeatures.id, Attribute::id).add(Attribute.RFeatures.many, Attribute::many).add(Attribute.RFeatures.mandatory, Attribute::mandatory).add(Attribute.RFeatures.parameters, Attribute::parameters).add(Attribute.RFeatures.rawFeature, Attribute::rawFeature).add(Attribute.RFeatures.datatype, Attribute::datatype).add(Attribute.RFeatures.defaultValue, Attribute::defaultValue).build();
  private static final FeatureSetter<Attribute<?, ?>> SET_MAP = new FeatureSetter.Builder<Attribute<?, ?>>().build();
  private final String name;
  private final boolean immutable;
  private final int id;
  private final boolean many;
  private final boolean mandatory;
  private final List<GenericParameter> parameters;
  private final RawFeature<UnaryType, EffectiveType> rawFeature;
  private final Datatype<UnaryType> datatype;
  private final String defaultValue;

  public AttributeImpl(final String name, final boolean immutable, final int id, final boolean many,
      final boolean mandatory, final List<GenericParameter> parameters,
      final RawFeature<UnaryType, EffectiveType> rawFeature, final Datatype<UnaryType> datatype,
      final String defaultValue) {
    this.name = name;
    this.immutable = immutable;
    this.id = id;
    this.many = many;
    this.mandatory = mandatory;
    this.parameters = List.copyOf(parameters);
    this.rawFeature = rawFeature;
    this.datatype = datatype;
    this.defaultValue = defaultValue;
    setContainer(parameters, Feature.RFeatures.parameters);
    eDeliver(true);
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
  public int id() {
    return id;
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
  public List<GenericParameter> parameters() {
    return parameters;
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
  public Group<Attribute<?, ?>> lmGroup() {
    return LMCoreModelDefinition.Groups.ATTRIBUTE;
  }

  @Override
  protected FeatureSetter<Attribute<?, ?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Attribute<?, ?>> getterMap() {
    return GET_MAP;
  }

  @Override
  protected int featureIndex(int featureId) {
    return switch (featureId) {
      case Attribute.FeatureIDs.NAME -> 0;
      case Attribute.FeatureIDs.IMMUTABLE -> 1;
      case Attribute.FeatureIDs.ID -> 2;
      case Attribute.FeatureIDs.MANY -> 3;
      case Attribute.FeatureIDs.MANDATORY -> 4;
      case Attribute.FeatureIDs.PARAMETERS -> 5;
      case Attribute.FeatureIDs.RAW_FEATURE -> 6;
      case Attribute.FeatureIDs.DATATYPE -> 7;
      case Attribute.FeatureIDs.DEFAULT_VALUE -> 8;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
