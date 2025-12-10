package org.logoce.lmf.model.lang.builder;

import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.lang.Unit.Builder;
import org.logoce.lmf.model.lang.impl.UnitImpl;

public final class UnitBuilder<T> implements Builder<T> {
  private String name;
  private String matcher;
  private String defaultValue;
  private Primitive primitive = Primitive.String;
  private String extractor;

  public UnitBuilder() {
  }

  @Override
  public UnitBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public UnitBuilder<T> matcher(String matcher) {
    this.matcher = matcher;
    return this;
  }

  @Override
  public UnitBuilder<T> defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public UnitBuilder<T> primitive(Primitive primitive) {
    this.primitive = primitive;
    return this;
  }

  @Override
  public UnitBuilder<T> extractor(String extractor) {
    this.extractor = extractor;
    return this;
  }

  @Override
  public Unit<T> build() {
    final var built = new UnitImpl<T>(name, matcher, defaultValue, primitive, extractor);
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
    private static final FeatureInserter<UnitBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<UnitBuilder>(5, Inserters::attributeIndex).add(Unit.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(Unit.FeatureIDs.MATCHER, (builder, value) -> builder.matcher((String) value)).add(Unit.FeatureIDs.DEFAULT_VALUE, (builder, value) -> builder.defaultValue((String) value)).add(Unit.FeatureIDs.PRIMITIVE, (builder, value) -> builder.primitive((Primitive) value)).add(Unit.FeatureIDs.EXTRACTOR, (builder, value) -> builder.extractor((String) value)).build();
    private static final RelationLazyInserter<UnitBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<UnitBuilder>(0, Inserters::relationIndex).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Unit.FeatureIDs.NAME -> 0;
        case Unit.FeatureIDs.MATCHER -> 1;
        case Unit.FeatureIDs.DEFAULT_VALUE -> 2;
        case Unit.FeatureIDs.PRIMITIVE -> 3;
        case Unit.FeatureIDs.EXTRACTOR -> 4;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
    }
  }
}
