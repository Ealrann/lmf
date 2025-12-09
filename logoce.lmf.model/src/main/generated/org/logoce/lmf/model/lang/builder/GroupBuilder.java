package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Group.Builder;
import org.logoce.lmf.model.lang.Include;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.GroupImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class GroupBuilder<T extends LMObject> implements Builder<T> {
  private static final FeatureInserter<GroupBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GroupBuilder<?>>().add(Group.RFeatures.name, GroupBuilder::name).add(Group.RFeatures.concrete, GroupBuilder::concrete).add(Group.RFeatures.lmBuilder, GroupBuilder::_lmBuilder).build();
  private static final RelationLazyInserter<GroupBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<GroupBuilder<?>>().add(Group.RFeatures.includes, GroupBuilder::addInclude).add(Group.RFeatures.features, GroupBuilder::addFeature).add(Group.RFeatures.generics, GroupBuilder::addGeneric).add(Group.RFeatures.operations, GroupBuilder::addOperation).build();
  private String name;
  private boolean concrete;
  private final List<Supplier<Include<?>>> includes = new ArrayList<>();
  private final List<Supplier<Feature<?, ?>>> features = new ArrayList<>();
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
    includes.stream().map(value -> (Supplier<Include<?>>) () -> value).forEach(this.includes::add);
    return this;
  }

  @Override
  public GroupBuilder<T> addFeature(Supplier<Feature<?, ?>> feature) {
    this.features.add(feature);
    return this;
  }

  @Override
  public GroupBuilder<T> addFeatures(final List<Feature<?, ?>> features) {
    features.stream().map(value -> (Supplier<Feature<?, ?>>) () -> value).forEach(this.features::add);
    return this;
  }

  @Override
  public GroupBuilder<T> addGeneric(Supplier<Generic<?>> generic) {
    this.generics.add(generic);
    return this;
  }

  @Override
  public GroupBuilder<T> addGenerics(final List<Generic<?>> generics) {
    generics.stream().map(value -> (Supplier<Generic<?>>) () -> value).forEach(this.generics::add);
    return this;
  }

  @Override
  public GroupBuilder<T> addOperation(Supplier<Operation> operation) {
    this.operations.add(operation);
    return this;
  }

  @Override
  public GroupBuilder<T> addOperations(final List<Operation> operations) {
    operations.stream().map(value -> (Supplier<Operation>) () -> value).forEach(this.operations::add);
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
