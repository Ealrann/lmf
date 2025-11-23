package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMEntity;

public final class GenericExtensionImpl extends FeaturedObject implements GenericExtension {
  private static final FeatureGetter<GenericExtension> GET_MAP = new FeatureGetter.Builder<GenericExtension>().add(org.logoce.lmf.model.lang.GenericExtension.Features.type, org.logoce.lmf.model.lang.GenericExtension::type).add(org.logoce.lmf.model.lang.GenericExtension.Features.boundType, org.logoce.lmf.model.lang.GenericExtension::boundType).add(org.logoce.lmf.model.lang.GenericExtension.Features.parameter, org.logoce.lmf.model.lang.GenericExtension::parameter).build();
  private static final FeatureSetter<GenericExtension> SET_MAP = new FeatureSetter.Builder<GenericExtension>().build();
  private final LMEntity<?> type;
  private final BoundType boundType;
  private final GenericParameter parameter;

  public GenericExtensionImpl(final LMEntity<?> type, final BoundType boundType,
      final GenericParameter parameter) {
    this.type = type;
    this.boundType = boundType;
    this.parameter = parameter;
    setContainer(parameter, GenericExtension.Features.parameter);
  }

  @Override
  public LMEntity<?> type() {
    return type;
  }

  @Override
  public BoundType boundType() {
    return boundType;
  }

  @Override
  public GenericParameter parameter() {
    return parameter;
  }

  @Override
  public Group<GenericExtension> lmGroup() {
    return LMCoreDefinition.Groups.GENERIC_EXTENSION;
  }

  @Override
  protected FeatureSetter<GenericExtension> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<GenericExtension> getterMap() {
    return GET_MAP;
  }
}
