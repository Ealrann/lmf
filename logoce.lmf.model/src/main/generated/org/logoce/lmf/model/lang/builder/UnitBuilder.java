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
  private static final FeatureInserter<UnitBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<UnitBuilder<?>>().add(Unit.RFeatures.name, UnitBuilder::name).add(Unit.RFeatures.matcher, UnitBuilder::matcher).add(Unit.RFeatures.defaultValue, UnitBuilder::defaultValue).add(Unit.RFeatures.primitive, UnitBuilder::primitive).add(Unit.RFeatures.extractor, UnitBuilder::extractor).build();
  private static final RelationLazyInserter<UnitBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<UnitBuilder<?>>().build();
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
    ATTRIBUTE_INSERTER.push(this, attribute.rawFeature(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
      final Supplier<RelationType> supplier) {
    RELATION_INSERTER.push(this, relation.rawFeature(), supplier);
  }
}
