package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.feature.FeatureInserter;
import isotropy.lmf.core.feature.RelationLazyInserter;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Concept;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Reference;
import isotropy.lmf.core.lang.Reference.Builder;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.impl.ReferenceImpl;
import java.lang.Override;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class ReferenceBuilder<T extends LMObject> implements Builder<T> {
  private static final FeatureInserter<ReferenceBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<ReferenceBuilder<?>>().build();

  private static final RelationLazyInserter<ReferenceBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<ReferenceBuilder<?>>().add(Reference.Features.group, ReferenceBuilder::_group).add(Reference.Features.parameters, ReferenceBuilder::addParameter).build();

  private Supplier<Concept<T>> group;

  private final List<Supplier<Concept<?>>> parameters = new ArrayList<>();

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
    return new ReferenceImpl<>(group, parameters);
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
