package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
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
import org.logoce.lmf.model.util.BuildUtils;

public final class RelationBuilder<UnaryType extends LMObject, EffectiveType> implements Builder<UnaryType, EffectiveType> {
  private static final FeatureInserter<RelationBuilder<?, ?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<RelationBuilder<?, ?>>().add(Relation.RFeatures.name, RelationBuilder::name).add(Relation.RFeatures.immutable, RelationBuilder::immutable).add(Relation.RFeatures.id, RelationBuilder::id).add(Relation.RFeatures.many, RelationBuilder::many).add(Relation.RFeatures.mandatory, RelationBuilder::mandatory).add(Relation.RFeatures.rawFeature, RelationBuilder::_rawFeature).add(Relation.RFeatures.lazy, RelationBuilder::lazy).add(Relation.RFeatures.contains, RelationBuilder::contains).build();
  private static final RelationLazyInserter<RelationBuilder<?, ?>> RELATION_INSERTER = new RelationLazyInserter.Builder<RelationBuilder<?, ?>>().add(Relation.RFeatures.parameters, RelationBuilder::addParameter).add(Relation.RFeatures.concept, RelationBuilder::_concept).build();
  private String name;
  private boolean immutable;
  private int id;
  private boolean many;
  private boolean mandatory;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();
  private RawFeature<UnaryType, EffectiveType> rawFeature;
  private Supplier<Concept<UnaryType>> concept;
  private boolean lazy;
  private boolean contains;

  public RelationBuilder() {
  }

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
  public RelationBuilder<UnaryType, EffectiveType> id(int id) {
    this.id = id;
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
  public RelationBuilder<UnaryType, EffectiveType> addParameters(
      final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
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
    final var built = new RelationImpl<UnaryType, EffectiveType>(name, immutable, id, many, mandatory, builtParameters, rawFeature, concept, lazy, contains);
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
