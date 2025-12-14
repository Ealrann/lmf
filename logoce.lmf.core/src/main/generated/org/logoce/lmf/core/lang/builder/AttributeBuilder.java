package org.logoce.lmf.core.lang.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.feature.FeatureInserter;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Attribute.Builder;
import org.logoce.lmf.core.lang.Datatype;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.impl.AttributeImpl;
import org.logoce.lmf.core.util.BuildUtils;

public final class AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> implements Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> {
  private String name;
  private boolean immutable;
  private int id;
  private boolean many;
  private boolean mandatory;
  private final List<Supplier<GenericParameter>> parameters = new ArrayList<>();
  private Supplier<Datatype<UnaryType>> datatype;
  private String defaultValue;

  public AttributeBuilder() {
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> immutable(
      boolean immutable) {
    this.immutable = immutable;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> id(int id) {
    this.id = id;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> many(boolean many) {
    this.many = many;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> mandatory(
      boolean mandatory) {
    this.mandatory = mandatory;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameter(
      Supplier<GenericParameter> parameter) {
    this.parameters.add(parameter);
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameters(
      final List<GenericParameter> parameters) {
    parameters.stream().map(value -> (Supplier<GenericParameter>) () -> value).forEach(this.parameters::add);
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> datatype(
      Supplier<Datatype<UnaryType>> datatype) {
    this.datatype = datatype;
    return this;
  }

  @SuppressWarnings({
      "unchecked",
      "rawtypes"
  })
  private AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> _datatype(
      final Supplier<Datatype<?>> datatype) {
    this.datatype = (Supplier) datatype;
    return this;
  }

  @Override
  public AttributeBuilder<UnaryType, EffectiveType, ListenerType, ParentGroup> defaultValue(
      String defaultValue) {
    this.defaultValue = defaultValue;
    return this;
  }

  @Override
  public Attribute<UnaryType, EffectiveType, ListenerType, ParentGroup> build() {
    final var builtParameters = BuildUtils.collectSuppliers(parameters);
    final var built = new AttributeImpl<UnaryType, EffectiveType, ListenerType, ParentGroup>(name, immutable, id, many, mandatory, builtParameters, datatype, defaultValue);
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
    private static final FeatureInserter<AttributeBuilder> ATTRIBUTE_INSERTER = new FeatureInserter.Builder<AttributeBuilder>(6, Inserters::attributeIndex).add(Attribute.FeatureIDs.NAME, (builder, value) -> builder.name((String) value)).add(Attribute.FeatureIDs.IMMUTABLE, (builder, value) -> builder.immutable((boolean) value)).add(Attribute.FeatureIDs.ID, (builder, value) -> builder.id((int) value)).add(Attribute.FeatureIDs.MANY, (builder, value) -> builder.many((boolean) value)).add(Attribute.FeatureIDs.MANDATORY, (builder, value) -> builder.mandatory((boolean) value)).add(Attribute.FeatureIDs.DEFAULT_VALUE, (builder, value) -> builder.defaultValue((String) value)).build();
    private static final RelationLazyInserter<AttributeBuilder> RELATION_INSERTER = new RelationLazyInserter.Builder<AttributeBuilder>(2, Inserters::relationIndex).add(Attribute.FeatureIDs.PARAMETERS, (builder, value) -> builder.addParameter((Supplier<GenericParameter>) value)).add(Attribute.FeatureIDs.DATATYPE, (builder, value) -> builder._datatype((Supplier<Datatype<?>>) value)).build();

    private static int attributeIndex(final int featureId) {
      return switch (featureId) {
        case Attribute.FeatureIDs.NAME -> 0;
        case Attribute.FeatureIDs.IMMUTABLE -> 1;
        case Attribute.FeatureIDs.ID -> 2;
        case Attribute.FeatureIDs.MANY -> 3;
        case Attribute.FeatureIDs.MANDATORY -> 4;
        case Attribute.FeatureIDs.DEFAULT_VALUE -> 5;
        default -> throw new IllegalArgumentException("Unknown attribute featureId: " + featureId);
      };
    }

    private static int relationIndex(final int featureId) {
      return switch (featureId) {
        case Attribute.FeatureIDs.PARAMETERS -> 0;
        case Attribute.FeatureIDs.DATATYPE -> 1;
        default -> throw new IllegalArgumentException("Unknown relation featureId: " + featureId);
      };
    }
  }
}
