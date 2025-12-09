package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Type;

public final class GenericParameterImpl extends FeaturedObject implements GenericParameter {
  private static final FeatureGetter<GenericParameter> GET_MAP = new FeatureGetter.Builder<GenericParameter>().add(GenericParameter.Features.wildcard, GenericParameter::wildcard).add(GenericParameter.Features.wildcardBoundType, GenericParameter::wildcardBoundType).add(GenericParameter.Features.type, GenericParameter::type).add(GenericParameter.Features.parameters, GenericParameter::parameters).build();
  private static final FeatureSetter<GenericParameter> SET_MAP = new FeatureSetter.Builder<GenericParameter>().build();
  private final boolean wildcard;
  private final BoundType wildcardBoundType;
  private final Supplier<Type<?>> type;
  private final List<GenericParameter> parameters;

  public GenericParameterImpl(final boolean wildcard, final BoundType wildcardBoundType,
      final Supplier<Type<?>> type, final List<GenericParameter> parameters) {
    this.wildcard = wildcard;
    this.wildcardBoundType = wildcardBoundType;
    this.type = type;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, GenericParameter.Features.parameters);
    eDeliver(true);
  }

  @Override
  public boolean wildcard() {
    return wildcard;
  }

  @Override
  public BoundType wildcardBoundType() {
    return wildcardBoundType;
  }

  @Override
  public Type<?> type() {
    return type.get();
  }

  @Override
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<GenericParameter> lmGroup() {
    return LMCoreModelDefinition.Groups.GENERIC_PARAMETER;
  }

  @Override
  protected FeatureSetter<GenericParameter> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<GenericParameter> getterMap() {
    return GET_MAP;
  }

  @Override
  public int featureIndex(int featureId) {
    return switch (featureId) {
      case GenericParameter.FeatureIDs.WILDCARD -> 0;
      case GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE -> 1;
      case GenericParameter.FeatureIDs.TYPE -> 2;
      case GenericParameter.FeatureIDs.PARAMETERS -> 3;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
