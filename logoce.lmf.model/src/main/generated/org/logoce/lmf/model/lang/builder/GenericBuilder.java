package org.logoce.lmf.model.lang.builder;

import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Generic;
import org.logoce.lmf.model.lang.Generic.Builder;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.GenericImpl;

public final class GenericBuilder<T> implements Builder<T> {
  private static final FeatureInserter<GenericBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericBuilder<?>>().add(Generic.RFeatures.name, GenericBuilder::name).build();
  private static final RelationLazyInserter<GenericBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericBuilder<?>>().add(Generic.RFeatures.extension, GenericBuilder::extension).build();
  private String name;
  private Supplier<GenericExtension> extension = () -> null;

  public GenericBuilder() {
  }

  @Override
  public GenericBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public GenericBuilder<T> extension(Supplier<GenericExtension> extension) {
    this.extension = extension;
    return this;
  }

  @Override
  public Generic<T> build() {
    final var built = new GenericImpl<T>(name, extension.get());
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
