package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.GenericParameter.Builder;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.impl.GenericParameterImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class GenericParameterBuilder implements Builder {
  private static final FeatureInserter<GenericParameterBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericParameterBuilder>().add(GenericParameter.Features.wildcard, GenericParameterBuilder::wildcard).add(GenericParameter.Features.wildcardBoundType, GenericParameterBuilder::wildcardBoundType).build();
  private static final RelationLazyInserter<GenericParameterBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericParameterBuilder>().add(GenericParameter.Features.type, GenericParameterBuilder::type).add(GenericParameter.Features.parameters, GenericParameterBuilder::addParameter).build();
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
