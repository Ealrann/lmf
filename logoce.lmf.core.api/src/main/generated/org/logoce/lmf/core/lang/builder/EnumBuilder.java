package org.logoce.lmf.core.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.Enum.Builder;
import org.logoce.lmf.core.lang.EnumAttribute;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.impl.EnumImpl;
import org.logoce.lmf.core.util.BuildUtils;

public final class EnumBuilder<T> implements Builder<T> {
  private String name;
  private final List<String> literals = new ArrayList<>();
  private final List<Supplier<EnumAttribute>> attributes = new ArrayList<>();

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
  public EnumBuilder<T> addAttribute(Supplier<EnumAttribute> attribute) {
    this.attributes.add(attribute);
    return this;
  }

  @Override
  public EnumBuilder<T> addAttributes(final List<EnumAttribute> attributes) {
    attributes.forEach(value -> this.attributes.add(() -> value));
    return this;
  }

  @Override
  public Enum<T> build() {
    final var builtAttributes = BuildUtils.collectSuppliers(attributes);
    final var built = new EnumImpl<T>(name, literals, builtAttributes);
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
    private static final RelationLazyInserter<EnumBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<EnumBuilder>(1, Inserters::relationIndex).add(Enum.FeatureIDs.ATTRIBUTES, (builder, value) -> builder.addAttribute((Supplier<EnumAttribute>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Enum.FeatureIDs.NAME -> 0;
        case Enum.FeatureIDs.LITERALS -> 1;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case Enum.FeatureIDs.ATTRIBUTES -> 0;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
