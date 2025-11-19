package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Reference;
import org.logoce.lmf.model.util.BuildUtils;

public final class ReferenceImpl<T extends LMObject> extends FeaturedObject implements Reference<T> {
  private static final FeatureGetter<Reference<?>> GET_MAP = new FeatureGetter.Builder<Reference<?>>().add(org.logoce.lmf.model.lang.Reference.Features.group, org.logoce.lmf.model.lang.Reference::group).add(org.logoce.lmf.model.lang.Reference.Features.parameters, org.logoce.lmf.model.lang.Reference::parameters).build();
  private static final FeatureSetter<Reference<?>> SET_MAP = new FeatureSetter.Builder<Reference<?>>().build();
  private final Supplier<Concept<T>> group;
  private final List<Supplier<Concept<?>>> parameters;

  public ReferenceImpl(final Supplier<Concept<T>> group,
      final List<Supplier<Concept<?>>> parameters) {
    this.group = group;
    this.parameters = List.copyOf(parameters);
  }

  @Override
  public Concept<T> group() {
    return group.get();
  }

  @Override
  public List<Concept<?>> parameters() {
    return BuildUtils.collectSuppliers(parameters);
  }

  @Override
  public Group<Reference<?>> lmGroup() {
    return LMCoreDefinition.Groups.REFERENCE;
  }

  @Override
  protected FeatureSetter<Reference<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Reference<?>> getterMap() {
    return GET_MAP;
  }
}
