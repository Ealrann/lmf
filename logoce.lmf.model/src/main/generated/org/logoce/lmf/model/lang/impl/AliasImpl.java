package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;

public final class AliasImpl extends FeaturedObject implements Alias {
  private static final FeatureGetter<Alias> GET_MAP = new FeatureGetter.Builder<Alias>().add(org.logoce.lmf.model.lang.Alias.Features.name, org.logoce.lmf.model.lang.Alias::name).add(org.logoce.lmf.model.lang.Alias.Features.value, org.logoce.lmf.model.lang.Alias::value).build();

  private static final FeatureSetter<Alias> SET_MAP = new FeatureSetter.Builder<Alias>().build();

  private final String name;

  private final String value;

  public AliasImpl(final String name, final String value) {
    this.name = name;
    this.value = value;
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
    return LMCoreDefinition.Groups.ALIAS;
  }

  @Override
  protected FeatureSetter<Alias> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Alias> getterMap() {
    return GET_MAP;
  }
}
