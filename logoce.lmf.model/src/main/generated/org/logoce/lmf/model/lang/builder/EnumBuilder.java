package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Enum.Builder;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.EnumImpl;

public final class EnumBuilder<T> implements Builder<T> {
  private String name;
  private final List<String> literals = new ArrayList<>();

  public EnumBuilder() {
  }

  @Override
  public EnumBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public EnumBuilder<T> addLiteral(String literal) {
    this.literals.add(literal);
    return this;
  }

  @Override
  public EnumBuilder<T> addLiterals(final List<String> literals) {
    this.literals.addAll(literals);
    return this;
  }

  @Override
  public Enum<T> build() {
    final var built = new EnumImpl<T>(name, literals);
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
    private static final FeatureInserter<EnumBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<EnumBuilder>(2, Inserters::attributeIndex).add(Enum.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(Enum.FeatureIDs.LITERALS, (builder, value) -> builder.addLiteral((String) value)).build();
    private static final RelationLazyInserter<EnumBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<EnumBuilder>(0, Inserters::relationIndex).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Enum.FeatureIDs.NAME -> 0;
        case Enum.FeatureIDs.LITERALS -> 1;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
    }
  }
}
