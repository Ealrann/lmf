package org.logoce.lmf.core.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.OperationParameter;
import org.logoce.lmf.core.lang.OperationParameter.Builder;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.Type;
import org.logoce.lmf.core.lang.impl.OperationParameterImpl;
import org.logoce.lmf.core.util.BuildUtils;

public final class OperationParameterBuilder implements Builder {
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
    parameters.forEach(value -> this.parameters.add(() -> value));
    return this;
  }

  @Override
  public OperationParameter build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new OperationParameterImpl(name, type, builtParameters);
    return built;
  }

  @Override
  public <AttributeType> void push(final Attribute<?, ?, ?, ?> attribute,
      final AttributeType value) {
    Inserters.ATTRIBUTE_INSERTER.push(this, attribute.id(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?, ?, ?> relation,
      final Supplier<RelationType> supplier) {
    Inserters.RELATION_INSERTER.push(this, relation.id(), supplier);
  }

  private static final class Inserters {
    private static final FeatureInserter<OperationParameterBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<OperationParameterBuilder>(1, Inserters::attributeIndex).add(OperationParameter.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).build();
    private static final RelationLazyInserter<OperationParameterBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<OperationParameterBuilder>(2, Inserters::relationIndex).add(OperationParameter.FeatureIDs.TYPE, (builder, value) -> builder.type((Supplier<Type<?>>) value)).add(OperationParameter.FeatureIDs.PARAMETERS, (builder, value) -> builder.addParameter((Supplier<GenericParameter>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case OperationParameter.FeatureIDs.NAME -> 0;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case OperationParameter.FeatureIDs.TYPE -> 0;
        case OperationParameter.FeatureIDs.PARAMETERS -> 1;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
