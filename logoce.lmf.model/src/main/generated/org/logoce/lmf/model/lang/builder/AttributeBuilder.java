package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Attribute.Builder;
import org.logoce.lmf.model.lang.Datatype;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.AttributeImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class AttributeBuilder<UnaryType, EffectiveType> implements Builder<UnaryType, EffectiveType> {
  private static final FeatureInserter<AttributeBuilder<?, ?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<AttributeBuilder<?, ?>>().add(Attribute.Features.name, AttributeBuilder::name).add(Attribute.Features.immutable, AttributeBuilder::immutable).add(Attribute.Features.many, AttributeBuilder::many).add(Attribute.Features.mandatory, AttributeBuilder::mandatory).add(Attribute.Features.rawFeature, AttributeBuilder::_rawFeature).add(Attribute.Features.defaultValue, AttributeBuilder::defaultValue).build();

  private static final RelationLazyInserter<AttributeBuilder<?, ?>> RELATION_INSERTER = new RelationLazyInserter.Builder<AttributeBuilder<?, ?>>().add(Attribute.Features.datatype, AttributeBuilder::_datatype).add(Attribute.Features.parameters, AttributeBuilder::addParameter).build();

  private String name;

  private boolean immutable;

  private boolean many;

  private boolean mandatory;

  private RawFeature<UnaryType, EffectiveType> rawFeature;

  private Supplier<Datatype<UnaryType>> datatype;

  private String defaultValue;

  private final List<Supplier<Generic<?>>> parameters = new ArrayList<>();

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> immutable(boolean immutable) {
    this.immutable = immutable;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> many(boolean many) {
    this.many = many;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> mandatory(boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> rawFeature(
      RawFeature<UnaryType, EffectiveType> rawFeature) {
    this.rawFeature = rawFeature;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private AttributeBuilder<UnaryType, EffectiveType> _rawFeature(final RawFeature rawFeature) {
    this.rawFeature = (RawFeature<UnaryType, EffectiveType>) rawFeature;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> datatype(
      Supplier<Datatype<UnaryType>> datatype) {
    this.datatype = datatype;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private AttributeBuilder<UnaryType, EffectiveType> _datatype(final Supplier datatype) {
    this.datatype = datatype;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType> addParameter(Supplier<Generic<?>> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public Attribute<UnaryType, EffectiveType> build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    return new AttributeImpl<>(name, immutable, many, mandatory, rawFeature, datatype.get(), defaultValue, builtParameters);
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
