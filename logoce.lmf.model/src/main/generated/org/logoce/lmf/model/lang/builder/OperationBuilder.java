package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.Operation.Builder;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.impl.OperationImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class OperationBuilder implements Builder {
  private static final FeatureInserter<OperationBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<OperationBuilder>().add(Operation.Features.name, OperationBuilder::name).add(Operation.Features.content, OperationBuilder::content).build();
  private static final RelationLazyInserter<OperationBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<OperationBuilder>().add(Operation.Features.returnType, OperationBuilder::returnType).add(Operation.Features.returnTypeParameters, OperationBuilder::addReturnTypeParameter).add(Operation.Features.parameters, OperationBuilder::addParameter).build();
  private String name;
  private String content;
  private Supplier<Type<?>> returnType = () -> null;
  private final List<Supplier<GenericParameter>> returnTypeParameters = new ArrayList<>();
  private final List<Supplier<OperationParameter>> parameters = new ArrayList<>();

  public OperationBuilder() {
  }

  @Override
  public OperationBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public OperationBuilder content(String content) {
    this.content = content;
    return this;
  }

  @Override
  public OperationBuilder returnType(Supplier<Type<?>> returnType) {
    this.returnType = returnType;
    return this;
  }

  @Override
  public OperationBuilder addReturnTypeParameter(Supplier<GenericParameter> returnTypeParameter) {
    this.returnTypeParameters.add(returnTypeParameter);
    return this;
  }

  @Override
  public OperationBuilder addReturnTypeParameters(
      final List<GenericParameter> returnTypeParameters) {
    returnTypeParameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.returnTypeParameters::add);
    return this;
  }

  @Override
  public OperationBuilder addParameter(Supplier<OperationParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public OperationBuilder addParameters(final List<OperationParameter> parameters) {
    parameters.stream().map(value -> (Supplier<OperationParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public Operation build() {
    final var builtReturnTypeParameters = BuildUtils.collectSuppliers(returnTypeParameters);
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new OperationImpl(name, content, returnType, builtReturnTypeParameters, builtParameters);
    return built;
  }

  @Override
  public <AttributeType> void push(final Attribute<AttributeType, ?> attribute,
      final AttributeType value) {
    ATTRIBUTE_INSERTER.push(this, attribute.rawFeature(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
      final Supplier<RelationType> supplier) {
    RELATION_INSERTER.push(this, relation.rawFeature(), supplier);
  }
}
