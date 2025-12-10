package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
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

public final class RelationImpl<UnaryType extends LMObject, EffectiveType, ListenerType, ParentGroup extends LMObject> extends FeaturedObject implements Relation<UnaryType, EffectiveType, ListenerType, ParentGroup> {
  private final String name;
  private final boolean immutable;
  private final int id;
  private final boolean many;
  private final boolean mandatory;
  private final List<GenericParameter> parameters;
  private final Supplier<Concept<UnaryType>> concept;
  private final boolean lazy;
  private final boolean contains;

  public RelationImpl(final String name, final boolean immutable, final int id, final boolean many,
      final boolean mandatory, final List<GenericParameter> parameters,
      final Supplier<Concept<UnaryType>> concept, final boolean lazy, final boolean contains) {
    this.name = name;
    this.immutable = immutable;
    this.id = id;
    this.many = many;
    this.mandatory = mandatory;
    this.parameters = List.copyOf(parameters);
    this.concept = concept;
    this.lazy = lazy;
    this.contains = contains;
    setContainer(parameters, Feature.FeatureIDs.PARAMETERS);
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
  public Group<Relation<?, ?, ?, ?>> lmGroup() {
    return LMCoreModelDefinition.Groups.RELATION;
  }

  @Override
  protected FeatureSetter<Relation<?, ?, ?, ?>> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Relation<?, ?, ?, ?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Relation.FeatureIDs.NAME -> 0;
      case Relation.FeatureIDs.IMMUTABLE -> 1;
      case Relation.FeatureIDs.ID -> 2;
      case Relation.FeatureIDs.MANY -> 3;
      case Relation.FeatureIDs.MANDATORY -> 4;
      case Relation.FeatureIDs.PARAMETERS -> 5;
      case Relation.FeatureIDs.CONCEPT -> 6;
      case Relation.FeatureIDs.LAZY -> 7;
      case Relation.FeatureIDs.CONTAINS -> 8;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Relation<?, ?, ?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?, ?, ?>>(9, RelationImpl::featureIndexStatic).add(Relation.FeatureIDs.NAME, Relation::name).add(Relation.FeatureIDs.IMMUTABLE, Relation::immutable).add(Relation.FeatureIDs.ID, Relation::id).add(Relation.FeatureIDs.MANY, Relation::many).add(Relation.FeatureIDs.MANDATORY, Relation::mandatory).add(Relation.FeatureIDs.PARAMETERS, Relation::parameters).add(Relation.FeatureIDs.CONCEPT, Relation::concept).add(Relation.FeatureIDs.LAZY, Relation::lazy).add(Relation.FeatureIDs.CONTAINS, Relation::contains).build();
    private static final FeatureSetter<Relation<?, ?, ?, ?>> SET_MAP = new FeatureSetter.Builder<Relation<?, ?, ?, ?>>(9, RelationImpl::featureIndexStatic).build();
  }
}
