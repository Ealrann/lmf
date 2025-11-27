package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Enum.Builder;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.EnumImpl;

public final class EnumBuilder<T> implements Builder<T> {
  private static final FeatureInserter<EnumBuilder<?>> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<EnumBuilder<?>>().add(Enum.Features.name, EnumBuilder::name).add(Enum.Features.literals, EnumBuilder::addLiteral).build();
  private static final RelationLazyInserter<EnumBuilder<?>> RELATION_INSERTER = new RelationLazyInserter.Builder<EnumBuilder<?>>().build();
  private String name;
  private final List<String> literals = new ArrayList<>();

  @Override
  public EnumBuilder<T> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public EnumBuilder<T> addLiteral(String literal) {
    this.literals.add(literal);
    return this;
  }

  @Override
  public Enum<T> build() {
    final var built = new EnumImpl<T>(name, literals);
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
