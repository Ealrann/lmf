package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Type;

public final class OperationParameterImpl extends FeaturedObject implements OperationParameter {
  private static final FeatureGetter<OperationParameter> GET_MAP = new FeatureGetter.Builder<OperationParameter>().add(OperationParameter.Features.name, OperationParameter::name).add(OperationParameter.Features.type, OperationParameter::type).add(OperationParameter.Features.parameters, OperationParameter::parameters).build();
  private static final FeatureSetter<OperationParameter> SET_MAP = new FeatureSetter.Builder<OperationParameter>().build();
  private final String name;
  private final Supplier<Type<?>> type;
  private final List<GenericParameter> parameters;

  public OperationParameterImpl(final String name, final Supplier<Type<?>> type,
      final List<GenericParameter> parameters) {
    this.name = name;
    this.type = type;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, OperationParameter.Features.parameters);
    eDeliver(true);
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
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<OperationParameter> getterMap() {
    return GET_MAP;
  }

  @Override
  public int featureIndex(int featureId) {
    return switch (featureId) {
      case OperationParameter.FeatureIDs.NAME -> 0;
      case OperationParameter.FeatureIDs.TYPE -> 1;
      case OperationParameter.FeatureIDs.PARAMETERS -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
