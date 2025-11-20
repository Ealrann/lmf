package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Type;

public final class OperationImpl extends FeaturedObject implements Operation {
  private static final FeatureGetter<Operation> GET_MAP = new FeatureGetter.Builder<Operation>().add(org.logoce.lmf.model.lang.Operation.Features.name, org.logoce.lmf.model.lang.Operation::name).add(org.logoce.lmf.model.lang.Operation.Features.content, org.logoce.lmf.model.lang.Operation::content).add(org.logoce.lmf.model.lang.Operation.Features.type, org.logoce.lmf.model.lang.Operation::type).add(org.logoce.lmf.model.lang.Operation.Features.parameters, org.logoce.lmf.model.lang.Operation::parameters).build();
  private static final FeatureSetter<Operation> SET_MAP = new FeatureSetter.Builder<Operation>().build();
  private final String name;
  private final String content;
  private final Type<?> type;
  private final List<OperationParameter> parameters;

  public OperationImpl(final String name, final String content, final Type<?> type,
      final List<OperationParameter> parameters) {
    this.name = name;
    this.content = content;
    this.type = type;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, Operation.Features.parameters);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String content() {
    return content;
  }

  @Override
  public Type<?> type() {
    return type;
  }

  @Override
  public List<OperationParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<Operation> lmGroup() {
    return LMCoreDefinition.Groups.OPERATION;
  }

  @Override
  protected FeatureSetter<Operation> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Operation> getterMap() {
    return GET_MAP;
  }
}
