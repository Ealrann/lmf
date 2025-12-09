package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class AliasImpl extends FeaturedObject implements Alias {
  private static final FeatureGetter<Alias> GET_MAP = new FeatureGetter.Builder<Alias>().add(Alias.Features.name, Alias::name).add(Alias.Features.value, Alias::value).build();
  private static final FeatureSetter<Alias> SET_MAP = new FeatureSetter.Builder<Alias>().build();
  private final String name;
  private final String value;

  public AliasImpl(final String name, final String value) {
    this.name = name;
    this.value = value;
    eDeliver(true);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Group<Alias> lmGroup() {
    return LMCoreModelDefinition.Groups.ALIAS;
  }

  @Override
  protected FeatureSetter<Alias> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Alias> getterMap() {
    return GET_MAP;
  }

  @Override
  public int featureIndex(int featureId) {
    return switch (featureId) {
      case Alias.FeatureIDs.NAME -> 0;
      case Alias.FeatureIDs.VALUE -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
