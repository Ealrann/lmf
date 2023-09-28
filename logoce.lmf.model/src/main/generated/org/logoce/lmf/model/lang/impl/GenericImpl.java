package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Type;

public final class GenericImpl<T extends LMObject> extends FeaturedObject implements Generic<T> {
  private static final FeatureGetter<Generic<?>> GET_MAP = new FeatureGetter.Builder<Generic<?>>().add(org.logoce.lmf.model.lang.Generic.Features.name, org.logoce.lmf.model.lang.Generic::name).add(org.logoce.lmf.model.lang.Generic.Features.type, org.logoce.lmf.model.lang.Generic::type).add(org.logoce.lmf.model.lang.Generic.Features.boundType, org.logoce.lmf.model.lang.Generic::boundType).build();

  private static final FeatureSetter<Generic<?>> SET_MAP = new FeatureSetter.Builder<Generic<?>>().build();

  private final String name;

  private final Type<T> type;

  private final BoundType boundType;

  public GenericImpl(final String name, final Type<T> type, final BoundType boundType) {
    this.name = name;
    this.type = type;
    this.boundType = boundType;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Type<T> type() {
    return type;
  }

  @Override
  public BoundType boundType() {
    return boundType;
  }

  @Override
  public Group<Generic<?>> lmGroup() {
    return LMCoreDefinition.Groups.GENERIC;
  }

  @Override
  protected FeatureSetter<Generic<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Generic<?>> getterMap() {
    return GET_MAP;
  }
}
