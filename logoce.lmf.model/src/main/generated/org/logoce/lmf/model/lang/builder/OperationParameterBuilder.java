package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.OperationParameter.Builder;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.impl.OperationParameterImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class OperationParameterBuilder implements Builder {
  private static final FeatureInserter<OperationParameterBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<OperationParameterBuilder>().add(OperationParameter.Features.name, OperationParameterBuilder::name).build();
  private static final RelationLazyInserter<OperationParameterBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<OperationParameterBuilder>().add(OperationParameter.Features.type, OperationParameterBuilder::type).add(OperationParameter.Features.parameters, OperationParameterBuilder::addParameter).build();
  private String name;
  private Supplier<Type<?>> type;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();

  public OperationParameterBuilder() {
  }

  @Override
  public OperationParameterBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public OperationParameterBuilder type(Supplier<Type<?>> type) {
    this.type = type;
    return this;
  }

  @Override
  public OperationParameterBuilder addParameter(Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public OperationParameterBuilder addParameters(final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public OperationParameter build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new OperationParameterImpl(name, type, builtParameters);
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
