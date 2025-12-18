package org.logoce.lmf.core.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Generic;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Group.Builder;
import org.logoce.lmf.core.lang.Include;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Operation;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.impl.GroupImpl;
import org.logoce.lmf.core.util.BuildUtils;

public final class GroupBuilder<T extends LMObject> implements Builder<T> {
  private String name;
  private boolean concrete;
  private final List<Supplier<Include<?>>> includes = new ArrayList<>();
  private final List<Supplier<Feature<?, ?, ?, ?>>> features = new ArrayList<>();
  private final List<Supplier<Generic<?>>> generics = new ArrayList<>();
  private final List<Supplier<Operation>> operations = new ArrayList<>();
  private BuilderSupplier<T> lmBuilder;

  public GroupBuilder() {
  }

  @Override
  public GroupBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public GroupBuilder<T> concrete(boolean concrete) {
    this.concrete = concrete;
    return this;
  }

  @Override
  public GroupBuilder<T> addInclude(Supplier<Include<?>> include) {
    this.includes.add(include);
    return this;
  }

  @Override
  public GroupBuilder<T> addIncludes(final List<Include<?>> includes) {
    includes.forEach(value -> this.includes.add(() -> value));
    return this;
  }

  @Override
  public GroupBuilder<T> addFeature(Supplier<Feature<?, ?, ?, ?>> feature) {
    this.features.add(feature);
    return this;
  }

  @Override
  public GroupBuilder<T> addFeatures(final List<Feature<?, ?, ?, ?>> features) {
    features.forEach(value -> this.features.add(() -> value));
    return this;
  }

  @Override
  public GroupBuilder<T> addGeneric(Supplier<Generic<?>> generic) {
    this.generics.add(generic);
    return this;
  }

  @Override
  public GroupBuilder<T> addGenerics(final List<Generic<?>> generics) {
    generics.forEach(value -> this.generics.add(() -> value));
    return this;
  }

  @Override
  public GroupBuilder<T> addOperation(Supplier<Operation> operation) {
    this.operations.add(operation);
    return this;
  }

  @Override
  public GroupBuilder<T> addOperations(final List<Operation> operations) {
    operations.forEach(value -> this.operations.add(() -> value));
    return this;
  }

  @Override
  public GroupBuilder<T> lmBuilder(BuilderSupplier<T> lmBuilder) {
    this.lmBuilder = lmBuilder;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private GroupBuilder<T> _lmBuilder(final BuilderSupplier<?> lmBuilder) {
    this.lmBuilder = (BuilderSupplier<T>) lmBuilder;
    return this;
  }

  @Override
  public Group<T> build() {
    final var builtIncludes = BuildUtils.collectSuppliers(includes);
    final var builtFeatures = BuildUtils.collectSuppliers(features);
    final var builtGenerics = BuildUtils.collectSuppliers(generics);
    final var builtOperations = BuildUtils.collectSuppliers(operations);
    final var built = new GroupImpl<T>(name, concrete, builtIncludes, builtFeatures, builtGenerics, builtOperations, lmBuilder);
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
    private static final FeatureInserter<GroupBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GroupBuilder>(3, Inserters::attributeIndex).add(Group.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(Group.FeatureIDs.CONCRETE, (builder, value) -> builder.concrete((boolean) value)).add(Group.FeatureIDs.LM_BUILDER, (builder, value) -> builder._lmBuilder((BuilderSupplier<?>) value)).build();
    private static final RelationLazyInserter<GroupBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<GroupBuilder>(4, Inserters::relationIndex).add(Group.FeatureIDs.INCLUDES, (builder, value) -> builder.addInclude((Supplier<Include<?>>) value)).add(Group.FeatureIDs.FEATURES, (builder, value) -> builder.addFeature((Supplier<Feature<?, ?, ?, ?>>) value)).add(Group.FeatureIDs.GENERICS, (builder, value) -> builder.addGeneric((Supplier<Generic<?>>) value)).add(Group.FeatureIDs.OPERATIONS, (builder, value) -> builder.addOperation((Supplier<Operation>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Group.FeatureIDs.NAME -> 0;
        case Group.FeatureIDs.CONCRETE -> 1;
        case Group.FeatureIDs.LM_BUILDER -> 2;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case Group.FeatureIDs.INCLUDES -> 0;
        case Group.FeatureIDs.FEATURES -> 1;
        case Group.FeatureIDs.GENERICS -> 2;
        case Group.FeatureIDs.OPERATIONS -> 3;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
