package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType> {
  private static final FeatureGetter<Relation<?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?>>().add(Relation.RFeatures.name, Relation::name).add(Relation.RFeatures.immutable, Relation::immutable).add(Relation.RFeatures.id, Relation::id).add(Relation.RFeatures.many, Relation::many).add(Relation.RFeatures.mandatory, Relation::mandatory).add(Relation.RFeatures.parameters, Relation::parameters).add(Relation.RFeatures.rawFeature, Relation::rawFeature).add(Relation.RFeatures.concept, Relation::concept).add(Relation.RFeatures.lazy, Relation::lazy).add(Relation.RFeatures.contains, Relation::contains).build();
  private static final FeatureSetter<Relation<?, ?>> SET_MAP = new FeatureSetter.Builder<Relation<?, ?>>().build();
  private final String name;
  private final boolean immutable;
  private final int id;
  private final boolean many;
  private final boolean mandatory;
  private final List<GenericParameter> parameters;
  private final RawFeature<UnaryType, EffectiveType> rawFeature;
  private final Supplier<Concept<UnaryType>> concept;
  private final boolean lazy;
  private final boolean contains;

  public RelationImpl(final String name, final boolean immutable, final int id, final boolean many,
      final boolean mandatory, final List<GenericParameter> parameters,
      final RawFeature<UnaryType, EffectiveType> rawFeature,
      final Supplier<Concept<UnaryType>> concept, final boolean lazy, final boolean contains) {
    this.name = name;
    this.immutable = immutable;
    this.id = id;
    this.many = many;
    this.mandatory = mandatory;
    this.parameters = List.copyOf(parameters);
    this.rawFeature = rawFeature;
    this.concept = concept;
    this.lazy = lazy;
    this.contains = contains;
    setContainer(parameters, Feature.RFeatures.parameters);
    eDeliver(true);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean immutable() {
    return immutable;
  }

  @Override
  public int id() {
    return id;
  }

  @Override
  public boolean many() {
    return many;
  }

  @Override
  public boolean mandatory() {
    return mandatory;
  }

  @Override
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public RawFeature<UnaryType, EffectiveType> rawFeature() {
    return rawFeature;
  }

  @Override
  public Concept<UnaryType> concept() {
    return concept.get();
  }

  @Override
  public boolean lazy() {
    return lazy;
  }

  @Override
  public boolean contains() {
    return contains;
  }

  @Override
  public Group<Relation<?, ?>> lmGroup() {
    return LMCoreModelDefinition.Groups.RELATION;
  }

  @Override
  protected FeatureSetter<Relation<?, ?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Relation<?, ?>> getterMap() {
    return GET_MAP;
  }

  @Override
  protected int featureIndex(int featureId) {
    return switch (featureId) {
      case Relation.FeatureIDs.NAME -> 0;
      case Relation.FeatureIDs.IMMUTABLE -> 1;
      case Relation.FeatureIDs.ID -> 2;
      case Relation.FeatureIDs.MANY -> 3;
      case Relation.FeatureIDs.MANDATORY -> 4;
      case Relation.FeatureIDs.PARAMETERS -> 5;
      case Relation.FeatureIDs.RAW_FEATURE -> 6;
      case Relation.FeatureIDs.CONCEPT -> 7;
      case Relation.FeatureIDs.LAZY -> 8;
      case Relation.FeatureIDs.CONTAINS -> 9;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
