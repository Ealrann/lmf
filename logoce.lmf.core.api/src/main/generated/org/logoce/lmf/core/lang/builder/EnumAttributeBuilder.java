package org.logoce.lmf.core.lang.builder;

import java.util.function.Supplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.EnumAttribute;
import org.logoce.lmf.core.lang.EnumAttribute.Builder;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.Unit;
import org.logoce.lmf.core.lang.impl.EnumAttributeImpl;

public final class EnumAttributeBuilder implements Builder {
  private String name;
  private Supplier<Unit<?>> unit;
  private String defaultValue;

  public EnumAttributeBuilder() {
  }

  @Override
  public EnumAttributeBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public EnumAttributeBuilder unit(Supplier<Unit<?>> unit) {
    this.unit = unit;
    return this;
  }

  @Override
  public EnumAttributeBuilder defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public EnumAttribute build() {
    final var built = new EnumAttributeImpl(name, unit.get(), defaultValue);
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
    private static final FeatureInserter<EnumAttributeBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<EnumAttributeBuilder>(2, Inserters::attributeIndex).add(EnumAttribute.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(EnumAttribute.FeatureIDs.DEFAULT_VALUE, (builder, value) -> builder.defaultValue((String) value)).build();
    private static final RelationLazyInserter<EnumAttributeBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<EnumAttributeBuilder>(1, Inserters::relationIndex).add(EnumAttribute.FeatureIDs.UNIT, (builder, value) -> builder.unit((Supplier<Unit<?>>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case EnumAttribute.FeatureIDs.NAME -> 0;
        case EnumAttribute.FeatureIDs.DEFAULT_VALUE -> 1;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case EnumAttribute.FeatureIDs.UNIT -> 0;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
