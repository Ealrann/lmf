package org.logoce.lmf.core.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.BoundType;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.GenericParameter.Builder;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.Type;
import org.logoce.lmf.core.lang.impl.GenericParameterImpl;
import org.logoce.lmf.core.util.BuildUtils;

public final class GenericParameterBuilder implements Builder {
  private boolean wildcard;
  private BoundType wildcardBoundType;
  private Supplier<Type<?>> type;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();

  public GenericParameterBuilder() {
  }

  @Override
  public GenericParameterBuilder wildcard(boolean wildcard) {
    this.wildcard = wildcard;
    return this;
  }

  @Override
  public GenericParameterBuilder wildcardBoundType(BoundType wildcardBoundType) {
    this.wildcardBoundType = wildcardBoundType;
    return this;
  }

  @Override
  public GenericParameterBuilder type(Supplier<Type<?>> type) {
    this.type = type;
    return this;
  }

  @Override
  public GenericParameterBuilder addParameter(Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public GenericParameterBuilder addParameters(final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public GenericParameter build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new GenericParameterImpl(wildcard, wildcardBoundType, type, builtParameters);
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
    private static final FeatureInserter<GenericParameterBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericParameterBuilder>(2, Inserters::attributeIndex).add(GenericParameter.FeatureIDs.WILDCARD, (builder, value) -> builder.wildcard((boolean) value)).add(GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE, (builder, value) -> builder.wildcardBoundType((BoundType) value)).build();
    private static final RelationLazyInserter<GenericParameterBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericParameterBuilder>(2, Inserters::relationIndex).add(GenericParameter.FeatureIDs.TYPE, (builder, value) -> builder.type((Supplier<Type<?>>) value)).add(GenericParameter.FeatureIDs.PARAMETERS, (builder, value) -> builder.addParameter((Supplier<GenericParameter>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case GenericParameter.FeatureIDs.WILDCARD -> 0;
        case GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE -> 1;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case GenericParameter.FeatureIDs.TYPE -> 0;
        case GenericParameter.FeatureIDs.PARAMETERS -> 1;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
