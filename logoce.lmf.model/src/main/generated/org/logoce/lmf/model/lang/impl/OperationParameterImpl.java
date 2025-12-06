package org.logoce.lmf.model.lang.impl;

import java.util.List;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Type;

public final class OperationParameterImpl extends FeaturedObject implements OperationParameter {
  private static final FeatureGetter<OperationParameter> GET_MAP = new FeatureGetter.Builder<OperationParameter>().add(org.logoce.lmf.model.lang.OperationParameter.Features.name, org.logoce.lmf.model.lang.OperationParameter::name).add(org.logoce.lmf.model.lang.OperationParameter.Features.type, org.logoce.lmf.model.lang.OperationParameter::type).add(org.logoce.lmf.model.lang.OperationParameter.Features.parameters, org.logoce.lmf.model.lang.OperationParameter::parameters).build();
  private static final FeatureSetter<OperationParameter> SET_MAP = new FeatureSetter.Builder<OperationParameter>().build();
  private final String name;
  private final Type<?> type;
  private final List<GenericParameter> parameters;

  public OperationParameterImpl(final String name, final Type<?> type,
      final List<GenericParameter> parameters) {
    this.name = name;
    this.type = type;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, OperationParameter.Features.parameters);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Type<?> type() {
    return type;
  }

  @Override
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<OperationParameter> lmGroup() {
    return LMCoreDefinition.Groups.OPERATION_PARAMETER;
  }

  @Override
  protected FeatureSetter<OperationParameter> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<OperationParameter> getterMap() {
    return GET_MAP;
  }
}
