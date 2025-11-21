package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.Serializer;

public final class SerializerImpl extends FeaturedObject implements Serializer {
  private static final FeatureGetter<Serializer> GET_MAP = new FeatureGetter.Builder<Serializer>().add(org.logoce.lmf.model.lang.Serializer.Features.toString, org.logoce.lmf.model.lang.Serializer::toString).add(org.logoce.lmf.model.lang.Serializer.Features.fromString, org.logoce.lmf.model.lang.Serializer::fromString).build();
  private static final FeatureSetter<Serializer> SET_MAP = new FeatureSetter.Builder<Serializer>().build();
  private final String toString;
  private final String fromString;

  public SerializerImpl(final String toString, final String fromString) {
    this.toString = toString;
    this.fromString = fromString;
  }

  @Override
  public String toString() {
    return toString;
  }

  @Override
  public String fromString() {
    return fromString;
  }

  @Override
  public Group<Serializer> lmGroup() {
    return LMCoreDefinition.Groups.SERIALIZER;
  }

  @Override
  protected FeatureSetter<Serializer> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Serializer> getterMap() {
    return GET_MAP;
  }
}
