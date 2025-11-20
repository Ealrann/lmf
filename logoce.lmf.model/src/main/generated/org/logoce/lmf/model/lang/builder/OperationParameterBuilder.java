package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.OperationParameter.Builder;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.impl.OperationParameterImpl;

public final class OperationParameterBuilder implements Builder {
  private static final FeatureInserter<OperationParameterBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<OperationParameterBuilder>().add(OperationParameter.Features.name, OperationParameterBuilder::name).build();
  private static final RelationLazyInserter<OperationParameterBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<OperationParameterBuilder>().add(OperationParameter.Features.type, OperationParameterBuilder::type).build();
  private String name;
  private Supplier<Type<?>> type;

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
  public OperationParameter build() {
    final var built = new OperationParameterImpl(name, type.get());
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
