package org.logoce.lmf.model.lang.builder;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.GenericExtension.Builder;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Type;
import org.logoce.lmf.model.lang.impl.GenericExtensionImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class GenericExtensionBuilder implements Builder {
  private static final FeatureInserter<GenericExtensionBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericExtensionBuilder>().add(GenericExtension.Features.boundType, GenericExtensionBuilder::boundType).build();
  private static final RelationLazyInserter<GenericExtensionBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericExtensionBuilder>().add(GenericExtension.Features.type, GenericExtensionBuilder::type).add(GenericExtension.Features.parameters, GenericExtensionBuilder::addParameter).build();
  private Supplier<Type<?>> type = () -> null;
  private BoundType boundType;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();

  @Override
  public GenericExtensionBuilder type(Supplier<Type<?>> type) {
    this.type = type;
    return this;
  }

  @Override
  public GenericExtensionBuilder boundType(BoundType boundType) {
    this.boundType = boundType;
    return this;
  }

  @Override
  public GenericExtensionBuilder addParameter(Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public GenericExtensionBuilder addParameters(final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public GenericExtension build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new GenericExtensionImpl(type, boundType, builtParameters);
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
