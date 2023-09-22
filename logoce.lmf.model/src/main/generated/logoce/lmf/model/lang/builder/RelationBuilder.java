package logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.function.Supplier;
import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.feature.FeatureInserter;
import logoce.lmf.model.feature.RelationLazyInserter;
import logoce.lmf.model.lang.Attribute;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.lang.Reference;
import logoce.lmf.model.lang.Relation;
import logoce.lmf.model.lang.Relation.Builder;
import logoce.lmf.model.lang.impl.RelationImpl;

public final class RelationBuilder<UnaryType extends LMObject, EffectiveType> implements Builder<UnaryType, EffectiveType> {
  private static final FeatureInserter<RelationBuilder<?, ?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<RelationBuilder<?, ?>>().add(Relation.Features.name, RelationBuilder::name).add(Relation.Features.immutable, RelationBuilder::immutable).add(Relation.Features.many, RelationBuilder::many).add(Relation.Features.mandatory, RelationBuilder::mandatory).add(Relation.Features.rawFeature, RelationBuilder::_rawFeature).add(Relation.Features.lazy, RelationBuilder::lazy).add(Relation.Features.contains, RelationBuilder::contains).build();

  private static final RelationLazyInserter<RelationBuilder<?, ?>> RELATION_INSERTER = new RelationLazyInserter.Builder<RelationBuilder<?, ?>>().add(Relation.Features.reference, RelationBuilder::_reference).build();

  private String name;

  private boolean immutable;

  private boolean many;

  private boolean mandatory;

  private RawFeature<UnaryType, EffectiveType> rawFeature;

  private Supplier<Reference<UnaryType>> reference;

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
  public RelationBuilder<UnaryType, EffectiveType> rawFeature(
      RawFeature<UnaryType, EffectiveType> rawFeature) {
    this.rawFeature = rawFeature;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private RelationBuilder<UnaryType, EffectiveType> _rawFeature(final RawFeature rawFeature) {
    this.rawFeature = (RawFeature<UnaryType, EffectiveType>) rawFeature;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType> reference(
      Supplier<Reference<UnaryType>> reference) {
    this.reference = reference;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private RelationBuilder<UnaryType, EffectiveType> _reference(final Supplier reference) {
    this.reference = reference;
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
    return new RelationImpl<>(name, immutable, many, mandatory, rawFeature, reference.get(), lazy, contains);
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
