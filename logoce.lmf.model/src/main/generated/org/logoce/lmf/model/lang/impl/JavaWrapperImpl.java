package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreDefinition;

public final class JavaWrapperImpl<T> extends FeaturedObject implements JavaWrapper<T> {
  private static final FeatureGetter<JavaWrapper<?>> GET_MAP = new FeatureGetter.Builder<JavaWrapper<?>>().add(org.logoce.lmf.model.lang.JavaWrapper.Features.name, org.logoce.lmf.model.lang.JavaWrapper::name).add(org.logoce.lmf.model.lang.JavaWrapper.Features.domain, org.logoce.lmf.model.lang.JavaWrapper::domain).build();
  private static final FeatureSetter<JavaWrapper<?>> SET_MAP = new FeatureSetter.Builder<JavaWrapper<?>>().build();
  private final String name;
  private final String domain;

  public JavaWrapperImpl(final String name, final String domain) {
    this.name = name;
    this.domain = domain;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String domain() {
    return domain;
  }

  @Override
  public Group<JavaWrapper<?>> lmGroup() {
    return LMCoreDefinition.Groups.JAVA_WRAPPER;
  }

  @Override
  protected FeatureSetter<JavaWrapper<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<JavaWrapper<?>> getterMap() {
    return GET_MAP;
  }
}
