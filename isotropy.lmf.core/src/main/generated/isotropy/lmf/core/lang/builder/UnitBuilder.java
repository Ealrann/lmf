package isotropy.lmf.core.lang.builder;

import isotropy.lmf.core.feature.FeatureInserter;
import isotropy.lmf.core.feature.RelationLazyInserter;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Primitive;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.Unit;
import isotropy.lmf.core.lang.Unit.Builder;
import isotropy.lmf.core.lang.impl.UnitImpl;
import java.lang.Override;
import java.lang.String;
import java.util.function.Supplier;

public final class UnitBuilder<T> implements Builder<T> {
  private static final FeatureInserter<UnitBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<UnitBuilder<?>>().add(Unit.Features.name, UnitBuilder::name).add(Unit.Features.matcher, UnitBuilder::matcher).add(Unit.Features.defaultValue, UnitBuilder::defaultValue).add(Unit.Features.primitive, UnitBuilder::primitive).add(Unit.Features.extractor, UnitBuilder::extractor).build();

  private static final RelationLazyInserter<UnitBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<UnitBuilder<?>>().build();

  private String name;

  private String matcher;

  private String defaultValue;

  private Primitive primitive = Primitive.String;

  private String extractor;

  @Override
  public UnitBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public UnitBuilder<T> matcher(String matcher) {
    this.matcher = matcher;
    return this;
  }

  @Override
  public UnitBuilder<T> defaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public UnitBuilder<T> primitive(Primitive primitive) {
    this.primitive = primitive;
    return this;
  }

  @Override
  public UnitBuilder<T> extractor(String extractor) {
    this.extractor = extractor;
    return this;
  }

  @Override
  public Unit<T> build() {
    return new UnitImpl<>(name, matcher, defaultValue, primitive, extractor);
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
