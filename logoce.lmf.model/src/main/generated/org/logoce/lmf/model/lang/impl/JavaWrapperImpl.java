package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Serializer;

public final class JavaWrapperImpl<T> extends FeaturedObject implements JavaWrapper<T> {
  private static final FeatureGetter<JavaWrapper<?>> GET_MAP = new FeatureGetter.Builder<JavaWrapper<?>>().add(JavaWrapper.Features.name, JavaWrapper::name).add(JavaWrapper.Features.qualifiedClassName, JavaWrapper::qualifiedClassName).add(JavaWrapper.Features.serializer, JavaWrapper::serializer).build();
  private static final FeatureSetter<JavaWrapper<?>> SET_MAP = new FeatureSetter.Builder<JavaWrapper<?>>().build();
  private final String name;
  private final String qualifiedClassName;
  private final Serializer serializer;

  public JavaWrapperImpl(final String name, final String qualifiedClassName,
      final Serializer serializer) {
    this.name = name;
    this.qualifiedClassName = qualifiedClassName;
    this.serializer = serializer;
    setContainer(serializer, JavaWrapper.Features.serializer);
    eDeliver(true);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String qualifiedClassName() {
    return qualifiedClassName;
  }

  @Override
  public Serializer serializer() {
    return serializer;
  }

  @Override
  public Group<JavaWrapper<?>> lmGroup() {
    return LMCoreModelDefinition.Groups.JAVA_WRAPPER;
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
