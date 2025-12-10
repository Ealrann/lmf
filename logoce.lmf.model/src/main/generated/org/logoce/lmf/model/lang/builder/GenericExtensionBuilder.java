package org.logoce.lmf.model.lang.builder;

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
  private Supplier<Type<?>> type = () -> null;
  private BoundType boundType;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();

  public GenericExtensionBuilder() {
  }

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
  public <AttributeType> void push(final Attribute<?, ?, ?, ?> attribute,
      final AttributeType value) {
    Inserters.ATTRIBUTE_INSERTER.push(this, attribute.id(), value);
  }

  @Override
  public <RelationType extends LMObject> void push(final Relation<RelationType, ?, ?, ?> relation,
      final Supplier<RelationType> supplier) {
    Inserters.RELATION_INSERTER.push(this, relation.id(), supplier);
  }

  private static final class Inserters {
    private static final FeatureInserter<GenericExtensionBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<GenericExtensionBuilder>(1, Inserters::attributeIndex).add(GenericExtension.FeatureIDs.BOUND_TYPE, (builder, value) -> builder.boundType((BoundType) value)).build();
    private static final RelationLazyInserter<GenericExtensionBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<GenericExtensionBuilder>(2, Inserters::relationIndex).add(GenericExtension.FeatureIDs.TYPE, (builder, value) -> builder.type((Supplier<Type<?>>) value)).add(GenericExtension.FeatureIDs.PARAMETERS, (builder, value) -> builder.addParameter((Supplier<GenericParameter>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case GenericExtension.FeatureIDs.BOUND_TYPE -> 0;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case GenericExtension.FeatureIDs.TYPE -> 0;
        case GenericExtension.FeatureIDs.PARAMETERS -> 1;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
