package org.logoce.lmf.core.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Concept;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.Relation.Builder;
import org.logoce.lmf.core.lang.impl.RelationImpl;
import org.logoce.lmf.core.util.BuildUtils;

public final class RelationBuilder<UnaryType extends LMObject, EffectiveType, ListenerType, ParentGroup> implements Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> {
  private String name;
  private boolean immutable;
  private int id;
  private boolean many;
  private boolean mandatory;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();
  private Supplier<Concept<UnaryType>> concept;
  private boolean lazy;
  private boolean contains;

  public RelationBuilder() {
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> immutable(
      boolean immutable) {
    this.immutable = immutable;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> id(int id) {
    this.id = id;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> many(boolean many) {
    this.many = many;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> mandatory(
      boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameter(
      Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameters(
      final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> concept(
      Supplier<Concept<UnaryType>> concept) {
    this.concept = concept;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> _concept(
      final Supplier<Concept<?>> concept) {
    this.concept = (Supplier) concept;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> lazy(boolean lazy) {
    this.lazy = lazy;
    return this;
  }

  @Override
  public RelationBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> contains(
      boolean contains) {
    this.contains = contains;
    return this;
  }

  @Override
  public Relation<UnaryType, EffectiveType, ListenerType, ParentGroup> build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new RelationImpl<UnaryType, EffectiveType, ListenerType, ParentGroup>(name, immutable, id, many, mandatory, builtParameters, concept, lazy, contains);
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
    private static final FeatureInserter<RelationBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<RelationBuilder>(7, Inserters::attributeIndex).add(Relation.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(Relation.FeatureIDs.IMMUTABLE, (builder, value) -> builder.immutable((boolean) value)).add(Relation.FeatureIDs.ID, (builder, value) -> builder.id((int) value)).add(Relation.FeatureIDs.MANY, (builder, value) -> builder.many((boolean) value)).add(Relation.FeatureIDs.MANDATORY, (builder, value) -> builder.mandatory((boolean) value)).add(Relation.FeatureIDs.LAZY, (builder, value) -> builder.lazy((boolean) value)).add(Relation.FeatureIDs.CONTAINS, (builder, value) -> builder.contains((boolean) value)).build();
    private static final RelationLazyInserter<RelationBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<RelationBuilder>(2, Inserters::relationIndex).add(Relation.FeatureIDs.PARAMETERS, (builder, value) -> builder.addParameter((Supplier<GenericParameter>) value)).add(Relation.FeatureIDs.CONCEPT, (builder, value) -> builder._concept((Supplier<Concept<?>>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Relation.FeatureIDs.NAME -> 0;
        case Relation.FeatureIDs.IMMUTABLE -> 1;
        case Relation.FeatureIDs.ID -> 2;
        case Relation.FeatureIDs.MANY -> 3;
        case Relation.FeatureIDs.MANDATORY -> 4;
        case Relation.FeatureIDs.LAZY -> 5;
        case Relation.FeatureIDs.CONTAINS -> 6;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case Relation.FeatureIDs.PARAMETERS -> 0;
        case Relation.FeatureIDs.CONCEPT -> 1;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
