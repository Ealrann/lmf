package org.logoce.lmf.model.lang.builder;

import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Alias.Builder;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.AliasImpl;

public final class AliasBuilder implements Builder {
  private String name;
  private String value;

  public AliasBuilder() {
  }

  @Override
  public AliasBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public AliasBuilder value(String value) {
    this.value = value;
    return this;
  }

  @Override
  public Alias build() {
    final var built = new AliasImpl(name, value);
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
    private static final FeatureInserter<AliasBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<AliasBuilder>(2, Inserters::attributeIndex).add(Alias.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(Alias.FeatureIDs.VALUE, (builder, value) -> builder.value((String) value)).build();
    private static final RelationLazyInserter<AliasBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<AliasBuilder>(0, Inserters::relationIndex).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Alias.FeatureIDs.NAME -> 0;
        case Alias.FeatureIDs.VALUE -> 1;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
    }
  }
}
