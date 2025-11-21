package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Serializer;
import org.logoce.lmf.model.lang.Serializer.Builder;
import org.logoce.lmf.model.lang.impl.SerializerImpl;

public final class SerializerBuilder implements Builder {
  private static final FeatureInserter<SerializerBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<SerializerBuilder>().add(Serializer.Features.toString, SerializerBuilder::toString).add(Serializer.Features.fromString, SerializerBuilder::fromString).build();
  private static final RelationLazyInserter<SerializerBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<SerializerBuilder>().build();
  private String toString;
  private String fromString;

  @Override
  public SerializerBuilder toString(String toString) {
    this.toString = toString;
    return this;
  }

  @Override
  public SerializerBuilder fromString(String fromString) {
    this.fromString = fromString;
    return this;
  }

  @Override
  public Serializer build() {
    final var built = new SerializerImpl(toString, fromString);
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
