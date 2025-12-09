package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Serializer;

public final class SerializerImpl extends FeaturedObject implements Serializer {
  private static final FeatureGetter<Serializer> GET_MAP = new FeatureGetter.Builder<Serializer>().add(Serializer.RFeatures.defaultValue, Serializer::defaultValue).add(Serializer.RFeatures.create, Serializer::create).add(Serializer.RFeatures.convert, Serializer::convert).build();
  private static final FeatureSetter<Serializer> SET_MAP = new FeatureSetter.Builder<Serializer>().build();
  private final String defaultValue;
  private final String create;
  private final String convert;

  public SerializerImpl(final String defaultValue, final String create, final String convert) {
    this.defaultValue = defaultValue;
    this.create = create;
    this.convert = convert;
    eDeliver(true);
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public String create() {
    return create;
  }

  @Override
  public String convert() {
    return convert;
  }

  @Override
  public Group<Serializer> lmGroup() {
    return LMCoreModelDefinition.Groups.SERIALIZER;
  }

  @Override
  protected FeatureSetter<Serializer> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Serializer> getterMap() {
    return GET_MAP;
  }

  @Override
  protected int featureIndex(int featureId) {
    return switch (featureId) {
      case Serializer.FeatureIDs.DEFAULT_VALUE -> 0;
      case Serializer.FeatureIDs.CREATE -> 1;
      case Serializer.FeatureIDs.CONVERT -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
