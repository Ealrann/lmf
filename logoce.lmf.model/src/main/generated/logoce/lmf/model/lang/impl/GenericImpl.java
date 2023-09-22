package logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import logoce.lmf.model.api.model.FeaturedObject;
import logoce.lmf.model.feature.FeatureGetter;
import logoce.lmf.model.feature.FeatureSetter;
import logoce.lmf.model.lang.BoundType;
import logoce.lmf.model.lang.Generic;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.LMCoreDefinition;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.lang.Type;

public final class GenericImpl<T extends LMObject> extends FeaturedObject implements Generic<T> {
  private static final FeatureGetter<Generic<?>> GET_MAP = new FeatureGetter.Builder<Generic<?>>().add(logoce.lmf.model.lang.Generic.Features.name, logoce.lmf.model.lang.Generic::name).add(logoce.lmf.model.lang.Generic.Features.type, logoce.lmf.model.lang.Generic::type).add(logoce.lmf.model.lang.Generic.Features.boundType, logoce.lmf.model.lang.Generic::boundType).build();

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
