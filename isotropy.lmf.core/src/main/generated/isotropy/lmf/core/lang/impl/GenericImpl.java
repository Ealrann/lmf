package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.BoundType;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Type;
import java.lang.Override;
import java.lang.String;

public final class GenericImpl<T extends LMObject> extends FeaturedObject implements Generic<T> {
  private static final FeatureGetter<Generic<?>> GET_MAP = new FeatureGetter.Builder<Generic<?>>().add(Features.name, Generic::name).add(Features.type, Generic::type).add(Features.boundType, Generic::boundType).build();

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
