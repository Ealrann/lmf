package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Include;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.LMObject;

public final class IncludeImpl<T extends LMObject> extends FeaturedObject implements Include<T> {
  private static final FeatureGetter<Include<?>> GET_MAP = new FeatureGetter.Builder<Include<?>>().add(Include.RFeatures.group, Include::group).add(Include.RFeatures.parameters, Include::parameters).build();
  private static final FeatureSetter<Include<?>> SET_MAP = new FeatureSetter.Builder<Include<?>>().build();
  private final Supplier<Group<T>> group;
  private final List<GenericParameter> parameters;

  public IncludeImpl(final Supplier<Group<T>> group, final List<GenericParameter> parameters) {
    this.group = group;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, Include.RFeatures.parameters);
    eDeliver(true);
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
    return LMCoreModelDefinition.Groups.INCLUDE;
  }

  @Override
  protected FeatureSetter<Include<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Include<?>> getterMap() {
    return GET_MAP;
  }

  @Override
  protected int featureIndex(int featureId) {
    return switch (featureId) {
      case Include.FeatureIDs.GROUP -> 0;
      case Include.FeatureIDs.PARAMETERS -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
