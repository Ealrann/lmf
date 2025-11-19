package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.SuppressWarnings;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Reference;
import org.logoce.lmf.model.lang.Reference.Builder;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.ReferenceImpl;
import org.logoce.lmf.model.notification.list.ObservableList;

public final class ReferenceBuilder<T extends LMObject> implements Builder<T> {
  private static final FeatureInserter<ReferenceBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<ReferenceBuilder<?>>().build();
  private static final RelationLazyInserter<ReferenceBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<ReferenceBuilder<?>>().add(Reference.Features.group, ReferenceBuilder::_group).add(Reference.Features.parameters, ReferenceBuilder::addParameter).build();
  private Supplier<Concept<T>> group;
  private final List<Supplier<Concept<?>>> parameters = new ObservableList<>((type, elements) -> {});

  @Override
  public ReferenceBuilder<T> group(Supplier<Concept<T>> group) {
    this.group = group;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private ReferenceBuilder<T> _group(final Supplier group) {
    this.group = group;
    return this;
  }

  @Override
  public ReferenceBuilder<T> addParameter(Supplier<Concept<?>> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public Reference<T> build() {
    final var built = new ReferenceImpl<T>(group, parameters);
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
