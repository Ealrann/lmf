package org.logoce.lmf.model.lang.builder;

import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Generic.Builder;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.GenericImpl;

public final class GenericBuilder<T> implements Builder<T> {
  private String name;
  private Supplier<GenericExtension> extension = () -> null;

  public GenericBuilder() {
  }

  @Override
  public GenericBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public GenericBuilder<T> extension(Supplier<GenericExtension> extension) {
    this.extension = extension;
    return this;
  }

  @Override
  public Generic<T> build() {
    final var built = new GenericImpl<T>(name, extension.get());
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
    private static final FeatureInserter<GenericBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericBuilder>(1, Inserters::attributeIndex).add(Generic.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).build();
    private static final RelationLazyInserter<GenericBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericBuilder>(1, Inserters::relationIndex).add(Generic.FeatureIDs.EXTENSION, (builder, value) -> builder.extension((Supplier<GenericExtension>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Generic.FeatureIDs.NAME -> 0;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case Generic.FeatureIDs.EXTENSION -> 0;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
