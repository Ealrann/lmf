package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Alias.Builder;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.AliasImpl;

public final class AliasBuilder implements Builder {
  private static final FeatureInserter<AliasBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<AliasBuilder>().add(Alias.Features.name, AliasBuilder::name).add(Alias.Features.value, AliasBuilder::value).build();

  private static final RelationLazyInserter<AliasBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<AliasBuilder>().build();

  private String name;

  private String value;

  @Override
  public AliasBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public AliasBuilder value(String value) {
    this.value = value;
    return this;
  }

  @Override
  public Alias build() {
    return new AliasImpl(name, value);
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
