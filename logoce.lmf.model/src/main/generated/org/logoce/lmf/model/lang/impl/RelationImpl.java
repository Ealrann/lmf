package org.logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreDefinition;
import org.logoce.lmf.model.lang.LMEntity;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.BuildUtils;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType> {
  private static final FeatureGetter<Relation<?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?>>().add(org.logoce.lmf.model.lang.Relation.Features.name, org.logoce.lmf.model.lang.Relation::name).add(org.logoce.lmf.model.lang.Relation.Features.immutable, org.logoce.lmf.model.lang.Relation::immutable).add(org.logoce.lmf.model.lang.Relation.Features.many, org.logoce.lmf.model.lang.Relation::many).add(org.logoce.lmf.model.lang.Relation.Features.mandatory, org.logoce.lmf.model.lang.Relation::mandatory).add(org.logoce.lmf.model.lang.Relation.Features.rawFeature, org.logoce.lmf.model.lang.Relation::rawFeature).add(org.logoce.lmf.model.lang.Relation.Features.concept, org.logoce.lmf.model.lang.Relation::concept).add(org.logoce.lmf.model.lang.Relation.Features.parameters, org.logoce.lmf.model.lang.Relation::parameters).add(org.logoce.lmf.model.lang.Relation.Features.lazy, org.logoce.lmf.model.lang.Relation::lazy).add(org.logoce.lmf.model.lang.Relation.Features.contains, org.logoce.lmf.model.lang.Relation::contains).build();
  private static final FeatureSetter<Relation<?, ?>> SET_MAP = new FeatureSetter.Builder<Relation<?, ?>>().build();
  private final String name;
  private final boolean immutable;
  private final boolean many;
  private final boolean mandatory;
  private final RawFeature<UnaryType, EffectiveType> rawFeature;
  private final Supplier<Concept<UnaryType>> concept;
  private final List<Supplier<LMEntity<?>>> parameters;
  private final boolean lazy;
  private final boolean contains;

  public RelationImpl(final String name, final boolean immutable, final boolean many,
      final boolean mandatory, final RawFeature<UnaryType, EffectiveType> rawFeature,
      final Supplier<Concept<UnaryType>> concept, final List<Supplier<LMEntity<?>>> parameters,
      final boolean lazy, final boolean contains) {
    this.name = name;
    this.immutable = immutable;
    this.many = many;
    this.mandatory = mandatory;
    this.rawFeature = rawFeature;
    this.concept = concept;
    this.parameters = List.copyOf(parameters);
    this.lazy = lazy;
    this.contains = contains;
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
  public RawFeature<UnaryType, EffectiveType> rawFeature() {
    return rawFeature;
  }

  @Override
  public Concept<UnaryType> concept() {
    return concept.get();
  }

  @Override
  public List<LMEntity<?>> parameters() {
    return BuildUtils.collectSuppliers(parameters);
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
