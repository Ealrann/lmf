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
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType> {
  private static final FeatureGetter<Relation<?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?>>().add(Relation.Features.name, Relation::name).add(Relation.Features.immutable, Relation::immutable).add(Relation.Features.many, Relation::many).add(Relation.Features.mandatory, Relation::mandatory).add(Relation.Features.parameters, Relation::parameters).add(Relation.Features.rawFeature, Relation::rawFeature).add(Relation.Features.concept, Relation::concept).add(Relation.Features.lazy, Relation::lazy).add(Relation.Features.contains, Relation::contains).build();
  private static final FeatureSetter<Relation<?, ?>> SET_MAP = new FeatureSetter.Builder<Relation<?, ?>>().build();
  private final String name;
  private final boolean immutable;
  private final boolean many;
  private final boolean mandatory;
  private final List<GenericParameter> parameters;
  private final RawFeature<UnaryType, EffectiveType> rawFeature;
  private final Supplier<Concept<UnaryType>> concept;
  private final boolean lazy;
  private final boolean contains;

  public RelationImpl(final String name, final boolean immutable, final boolean many,
      final boolean mandatory, final List<GenericParameter> parameters,
      final RawFeature<UnaryType, EffectiveType> rawFeature,
      final Supplier<Concept<UnaryType>> concept, final boolean lazy, final boolean contains) {
    this.name = name;
    this.immutable = immutable;
    this.many = many;
    this.mandatory = mandatory;
    this.parameters = List.copyOf(parameters);
    this.rawFeature = rawFeature;
    this.concept = concept;
    this.lazy = lazy;
    this.contains = contains;
    setContainer(parameters, Feature.Features.parameters);
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
    return LMCoreDefinition.Groups.RELATION;
  }

  @Override
  protected FeatureSetter<Relation<?, ?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Relation<?, ?>> getterMap() {
    return GET_MAP;
  }
}
