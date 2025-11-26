package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Relation.Builder;
import org.logoce.lmf.model.lang.impl.RelationImpl;
import org.logoce.lmf.model.notification.list.ObservableList;
import org.logoce.lmf.model.util.BuildUtils;

public final class RelationBuilder<UnaryType extends LMObject, EffectiveType> implements Builder<UnaryType, EffectiveType> {
  private static final FeatureInserter<RelationBuilder<?, ?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<RelationBuilder<?, ?>>().add(Relation.Features.name, RelationBuilder::name).add(Relation.Features.immutable, RelationBuilder::immutable).add(Relation.Features.many, RelationBuilder::many).add(Relation.Features.mandatory, RelationBuilder::mandatory).add(Relation.Features.rawFeature, RelationBuilder::_rawFeature).add(Relation.Features.lazy, RelationBuilder::lazy).add(Relation.Features.contains, RelationBuilder::contains).build();
  private static final RelationLazyInserter<RelationBuilder<?, ?>> RELATION_INSERTER = new RelationLazyInserter.Builder<RelationBuilder<?, ?>>().add(Relation.Features.parameters, RelationBuilder::addParameter).add(Relation.Features.concept, RelationBuilder::_concept).build();
  private String name;
  private boolean immutable;
  private boolean many;
  private boolean mandatory;
  private final List<Supplier<GenericParameter>> parameters = new ObservableList<>((type, elements) -> {});
  private RawFeature<UnaryType, EffectiveType> rawFeature;
  private Supplier<Concept<UnaryType>> concept;
  private boolean lazy;
  private boolean contains;

  @Override
  public RelationBuilder<UnaryType, EffectiveType> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> immutable(boolean immutable) {
    this.immutable = immutable;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> many(boolean many) {
    this.many = many;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> mandatory(boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> addParameter(
      Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> rawFeature(
      RawFeature<UnaryType, EffectiveType> rawFeature) {
    this.rawFeature = rawFeature;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private RelationBuilder<UnaryType, EffectiveType> _rawFeature(final RawFeature<?, ?> rawFeature) {
    this.rawFeature = (RawFeature<UnaryType, EffectiveType>) rawFeature;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> concept(Supplier<Concept<UnaryType>> concept) {
    this.concept = concept;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private RelationBuilder<UnaryType, EffectiveType> _concept(final Supplier<Concept<?>> concept) {
    this.concept = (Supplier) concept;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> lazy(boolean lazy) {
    this.lazy = lazy;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> contains(boolean contains) {
    this.contains = contains;
    return this;
  }

  @Override
  public Relation<UnaryType, EffectiveType> build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new RelationImpl<UnaryType, EffectiveType>(name, immutable, many, mandatory, builtParameters, rawFeature, concept, lazy, contains);
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
