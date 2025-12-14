package org.logoce.lmf.core.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.BoundType;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.Type;

public final class GenericParameterImpl extends FeaturedObject<GenericParameter.Features<?>> implements GenericParameter {
  private static final int FEATURE_COUNT = 4;
  private final ModelNotifier<GenericParameter.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
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
    setContainer(parameters, GenericParameter.FeatureIDs.PARAMETERS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<GenericParameter.Features<?>> notifier() {
    return notifier;
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
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<GenericParameter> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case GenericParameter.FeatureIDs.WILDCARD -> 0;
      case GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE -> 1;
      case GenericParameter.FeatureIDs.TYPE -> 2;
      case GenericParameter.FeatureIDs.PARAMETERS -> 3;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<GenericParameter> GET_MAP = new FeatureGetter.Builder<GenericParameter>(FEATURE_COUNT, GenericParameterImpl::featureIndexStatic).add(GenericParameter.FeatureIDs.WILDCARD, GenericParameter::wildcard).add(GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE, GenericParameter::wildcardBoundType).add(GenericParameter.FeatureIDs.TYPE, GenericParameter::type).add(GenericParameter.FeatureIDs.PARAMETERS, GenericParameter::parameters).build();
    private static final FeatureSetter<GenericParameter> SET_MAP = new FeatureSetter.Builder<GenericParameter>(FEATURE_COUNT, GenericParameterImpl::featureIndexStatic).build();
  }
}
