package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.BoundType;
import isotropy.lmf.core.lang.Generic;
import isotropy.lmf.core.lang.Generic.Builder;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.Type;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.model.FeatureInserter;
import isotropy.lmf.core.model.RelationLazyInserter;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.function.Supplier;

public final class GenericBuilder<T extends LMObject> implements Builder<T> {
  private static final FeatureInserter<GenericBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericBuilder<?>>().add(Generic.Features.name, GenericBuilder::name).add(Generic.Features.boundType, GenericBuilder::boundType).build();

  private static final RelationLazyInserter<GenericBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericBuilder<?>>().add(Generic.Features.type, GenericBuilder::_type).build();

  private String name;

  private Supplier<Type<T>> type = () -> null;

  private BoundType boundType;

  @Override
  public GenericBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public GenericBuilder<T> type(Supplier<Type<T>> type) {
    this.type = type;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private GenericBuilder<T> _type(final Supplier type) {
    this.type = type;
    return this;
  }

  @Override
  public GenericBuilder<T> boundType(BoundType boundType) {
    this.boundType = boundType;
    return this;
  }

  @Override
  public Generic<T> build() {
    return new GenericImpl<>(name, type.get(), boundType);
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
