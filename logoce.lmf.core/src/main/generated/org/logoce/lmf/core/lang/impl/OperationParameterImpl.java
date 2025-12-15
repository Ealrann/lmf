package org.logoce.lmf.core.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.OperationParameter;
import org.logoce.lmf.core.lang.Type;

public final class OperationParameterImpl extends FeaturedObject<OperationParameter.Features<?>> implements OperationParameter {
  private static final int FEATURE_COUNT = 3;
  private final ModelNotifier<OperationParameter.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final Supplier<Type<?>> type;
  private final List<GenericParameter> parameters;

  public OperationParameterImpl(final String name, final Supplier<Type<?>> type,
      final List<GenericParameter> parameters) {
    this.name = name;
    this.type = type;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, OperationParameter.FeatureIDs.PARAMETERS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<OperationParameter.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String name() {
    return name;
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
  public Group<OperationParameter> lmGroup() {
    return LMCoreModelDefinition.Groups.OPERATION_PARAMETER;
  }

  @Override
  protected FeatureSetter<OperationParameter> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<OperationParameter> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case OperationParameter.FeatureIDs.NAME -> 0;
      case OperationParameter.FeatureIDs.TYPE -> 1;
      case OperationParameter.FeatureIDs.PARAMETERS -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<OperationParameter> GET_MAP = new FeatureGetter.Builder<OperationParameter>(FEATURE_COUNT, OperationParameterImpl::featureIndexStatic).add(OperationParameter.FeatureIDs.NAME, OperationParameter::name).add(OperationParameter.FeatureIDs.TYPE, OperationParameter::type).add(OperationParameter.FeatureIDs.PARAMETERS, OperationParameter::parameters).build();
    private static final FeatureSetter<OperationParameter> SET_MAP = new FeatureSetter.Builder<OperationParameter>(FEATURE_COUNT, OperationParameterImpl::featureIndexStatic).build();
  }
}
