package org.logoce.lmf.model.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.feature.RelationLazyInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Include;
import org.logoce.lmf.model.lang.Include.Builder;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.impl.IncludeImpl;
import org.logoce.lmf.model.util.BuildUtils;

public final class IncludeBuilder<T extends LMObject> implements Builder<T> {
  private Supplier<Group<T>> group;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();

  public IncludeBuilder() {
  }

  @Override
  public IncludeBuilder<T> group(Supplier<Group<T>> group) {
    this.group = group;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private IncludeBuilder<T> _group(final Supplier<Group<?>> group) {
    this.group = (Supplier) group;
    return this;
  }

  @Override
  public IncludeBuilder<T> addParameter(Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public IncludeBuilder<T> addParameters(final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public Include<T> build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new IncludeImpl<T>(group, builtParameters);
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
    private static final FeatureInserter<IncludeBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<IncludeBuilder>(0, Inserters::attributeIndex).build();
    private static final RelationLazyInserter<IncludeBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<IncludeBuilder>(2, Inserters::relationIndex).add(Include.FeatureIDs.GROUP, (builder, value) -> builder._group((Supplier<Group<?>>) value)).add(Include.FeatureIDs.PARAMETERS, (builder, value) -> builder.addParameter((Supplier<GenericParameter>) value)).build();

    private static int attributeIndex(final int featureId) {
      throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case Include.FeatureIDs.GROUP -> 0;
        case Include.FeatureIDs.PARAMETERS -> 1;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
