package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.JavaWrapper;
import isotropy.lmf.core.lang.LMCoreDefinition;
import java.lang.Override;
import java.lang.String;

public final class JavaWrapperImpl<T> extends FeaturedObject implements JavaWrapper<T> {
  private static final FeatureGetter<JavaWrapper<?>> GET_MAP = new FeatureGetter.Builder<JavaWrapper<?>>().add(Features.name, JavaWrapper::name).add(Features.domain, JavaWrapper::domain).build();

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
