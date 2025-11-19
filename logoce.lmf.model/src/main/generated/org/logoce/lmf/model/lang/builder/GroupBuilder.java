package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Group.Builder;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Reference;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.GroupImpl;
import org.logoce.lmf.model.notification.list.ObservableList;
import org.logoce.lmf.model.util.BuildUtils;

public final class GroupBuilder<T extends LMObject> implements Builder<T> {
  private static final FeatureInserter<GroupBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GroupBuilder<?>>().add(Group.Features.name, GroupBuilder::name).add(Group.Features.concrete, GroupBuilder::concrete).build();
  private static final RelationLazyInserter<GroupBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<GroupBuilder<?>>().add(Group.Features.includes, GroupBuilder::addInclude).add(Group.Features.features, GroupBuilder::addFeature).add(Group.Features.generics, GroupBuilder::addGeneric).build();
  private String name;
  private boolean concrete;
  private final List<Supplier<Reference<?>>> includes = new ObservableList<>((type, elements) -> {});
  private final List<Supplier<Feature<?, ?>>> features = new ObservableList<>((type, elements) -> {});
  private final List<Supplier<Generic<?>>> generics = new ObservableList<>((type, elements) -> {});

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
  public GroupBuilder<T> addInclude(Supplier<Reference<?>> include) {
    this.includes.add(include);
    return this;
  }

  @Override
  public GroupBuilder<T> addFeature(Supplier<Feature<?, ?>> feature) {
    this.features.add(feature);
    return this;
  }

  @Override
  public GroupBuilder<T> addGeneric(Supplier<Generic<?>> generic) {
    this.generics.add(generic);
    return this;
  }

  @Override
  public Group<T> build() {
    final var builtIncludes = BuildUtils.collectSuppliers(includes);
    final var builtFeatures = BuildUtils.collectSuppliers(features);
    final var builtGenerics = BuildUtils.collectSuppliers(generics);
    final var built = new GroupImpl<T>(name, concrete, builtIncludes, builtFeatures, builtGenerics);
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
