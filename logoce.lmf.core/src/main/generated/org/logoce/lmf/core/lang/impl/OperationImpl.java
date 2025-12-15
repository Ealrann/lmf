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
import org.logoce.lmf.core.lang.Operation;
import org.logoce.lmf.core.lang.OperationParameter;
import org.logoce.lmf.core.lang.Type;

public final class OperationImpl extends FeaturedObject<Operation.Features<?>> implements Operation {
  private static final int FEATURE_COUNT = 5;
  private final ModelNotifier<Operation.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final String content;
  private final Supplier<Type<?>> returnType;
  private final List<GenericParameter> returnTypeParameters;
  private final List<OperationParameter> parameters;

  public OperationImpl(final String name, final String content, final Supplier<Type<?>> returnType,
      final List<GenericParameter> returnTypeParameters,
      final List<OperationParameter> parameters) {
    this.name = name;
    this.content = content;
    this.returnType = returnType;
    this.returnTypeParameters = List.copyOf(returnTypeParameters);
    this.parameters = List.copyOf(parameters);
    setContainer(returnTypeParameters, Operation.FeatureIDs.RETURN_TYPE_PARAMETERS);
    setContainer(parameters, Operation.FeatureIDs.PARAMETERS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Operation.Features<?>> notifier() {
    return notifier;
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
  public Type<?> returnType() {
    return returnType.get();
  }

  @Override
  public List<GenericParameter> returnTypeParameters() {
    return returnTypeParameters;
  }

  @Override
  public List<OperationParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<Operation> lmGroup() {
    return LMCoreModelDefinition.Groups.OPERATION;
  }

  @Override
  protected FeatureSetter<Operation> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Operation> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Operation.FeatureIDs.NAME -> 0;
      case Operation.FeatureIDs.CONTENT -> 1;
      case Operation.FeatureIDs.RETURN_TYPE -> 2;
      case Operation.FeatureIDs.RETURN_TYPE_PARAMETERS -> 3;
      case Operation.FeatureIDs.PARAMETERS -> 4;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Operation> GET_MAP = new FeatureGetter.Builder<Operation>(FEATURE_COUNT, OperationImpl::featureIndexStatic).add(Operation.FeatureIDs.NAME, Operation::name).add(Operation.FeatureIDs.CONTENT, Operation::content).add(Operation.FeatureIDs.RETURN_TYPE, Operation::returnType).add(Operation.FeatureIDs.RETURN_TYPE_PARAMETERS, Operation::returnTypeParameters).add(Operation.FeatureIDs.PARAMETERS, Operation::parameters).build();
    private static final FeatureSetter<Operation> SET_MAP = new FeatureSetter.Builder<Operation>(FEATURE_COUNT, OperationImpl::featureIndexStatic).build();
  }
}
