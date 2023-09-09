package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Group.Builder;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Reference;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.impl.GroupImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;
import isotropy.lmf.core.util.BuildUtils;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class GroupBuilder<T extends LMObject> implements Builder<T> {
  private static final FeatureInserter<GroupBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GroupBuilder<?>>().add(Group.Features.name, GroupBuilder::name).add(Group.Features.concrete, GroupBuilder::concrete).build();

  private static final RelationLazyInserter<GroupBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<GroupBuilder<?>>().add(Group.Features.includes, GroupBuilder::addInclude).add(Group.Features.features, GroupBuilder::addFeature).add(Group.Features.generics, GroupBuilder::addGeneric).build();

  private String name;

  private boolean concrete;

  private final List<Supplier<Reference<?>>> includes = new ArrayList<>();

  private final List<Supplier<Feature<?, ?>>> features = new ArrayList<>();

  private final List<Supplier<Generic<?>>> generics = new ArrayList<>();

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
    return new GroupImpl<>(name, concrete, builtIncludes, builtFeatures, builtGenerics);
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
