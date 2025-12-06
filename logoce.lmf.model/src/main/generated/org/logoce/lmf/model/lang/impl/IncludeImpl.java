package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Include;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMObject;

public final class IncludeImpl<T extends LMObject> extends FeaturedObject implements Include<T> {
  private static final FeatureGetter<Include<?>> GET_MAP = new FeatureGetter.Builder<Include<?>>().add(org.logoce.lmf.model.lang.Include.Features.group, org.logoce.lmf.model.lang.Include::group).add(org.logoce.lmf.model.lang.Include.Features.parameters, org.logoce.lmf.model.lang.Include::parameters).build();
  private static final FeatureSetter<Include<?>> SET_MAP = new FeatureSetter.Builder<Include<?>>().build();
  private final Supplier<Group<T>> group;
  private final List<GenericParameter> parameters;

  public IncludeImpl(final Supplier<Group<T>> group, final List<GenericParameter> parameters) {
    this.group = group;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, Include.Features.parameters);
  }

  @Override
  public Group<T> group() {
    return group.get();
  }

  @Override
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<Include<?>> lmGroup() {
    return LMCoreDefinition.Groups.INCLUDE;
  }

  @Override
  protected FeatureSetter<Include<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Include<?>> getterMap() {
    return GET_MAP;
  }
}
