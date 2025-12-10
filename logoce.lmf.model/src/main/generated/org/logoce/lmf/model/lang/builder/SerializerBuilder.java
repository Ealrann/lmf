package org.logoce.lmf.model.lang.builder;

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
  private String defaultValue;
  private String create;
  private String convert;

  public SerializerBuilder() {
  }

  @Override
  public SerializerBuilder defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public SerializerBuilder create(String create) {
    this.create = create;
    return this;
  }

  @Override
  public SerializerBuilder convert(String convert) {
    this.convert = convert;
    return this;
  }

  @Override
  public Serializer build() {
    final var built = new SerializerImpl(defaultValue, create, convert);
    return built;
  }

  @Override
  public <AttributeType> void push(final Attribute<AttributeType, ?> attribute,
      final AttributeType value) {
    Inserters.ATTRIBUTE_INSERTER.push(this, attribute.id(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
      final Supplier<RelationType> supplier) {
    Inserters.RELATION_INSERTER.push(this, relation.id(), supplier);
  }

  private static final class Inserters {
    private static final FeatureInserter<SerializerBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<SerializerBuilder>(3, Inserters::attributeIndex).add(Serializer.FeatureIDs.DEFAULT_VALUE, (builder, value) -> builder.defaultValue((String) value)).add(Serializer.FeatureIDs.CREATE, (builder, value) -> builder.create((String) value)).add(Serializer.FeatureIDs.CONVERT, (builder, value) -> builder.convert((String) value)).build();
    private static final RelationLazyInserter<SerializerBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<SerializerBuilder>(0, Inserters::relationIndex).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Serializer.FeatureIDs.DEFAULT_VALUE -> 0;
        case Serializer.FeatureIDs.CREATE -> 1;
        case Serializer.FeatureIDs.CONVERT -> 2;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
    }
  }
}
