package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Serializer;

public final class JavaWrapperImpl<T> extends FeaturedObject implements JavaWrapper<T> {
  private final String name;
  private final String qualifiedClassName;
  private final Serializer serializer;

  public JavaWrapperImpl(final String name, final String qualifiedClassName,
      final Serializer serializer) {
    this.name = name;
    this.qualifiedClassName = qualifiedClassName;
    this.serializer = serializer;
    setContainer(serializer, JavaWrapper.FeatureIDs.SERIALIZER);
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
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<JavaWrapper<?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case JavaWrapper.FeatureIDs.NAME -> 0;
      case JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME -> 1;
      case JavaWrapper.FeatureIDs.SERIALIZER -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<JavaWrapper<?>> GET_MAP = new FeatureGetter.Builder<JavaWrapper<?>>(3, JavaWrapperImpl::featureIndexStatic).add(JavaWrapper.FeatureIDs.NAME, JavaWrapper::name).add(JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME, JavaWrapper::qualifiedClassName).add(JavaWrapper.FeatureIDs.SERIALIZER, JavaWrapper::serializer).build();
    private static final FeatureSetter<JavaWrapper<?>> SET_MAP = new FeatureSetter.Builder<JavaWrapper<?>>(3, JavaWrapperImpl::featureIndexStatic).build();
  }
}
