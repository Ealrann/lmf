package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Reference;
import isotropy.lmf.core.lang.Relation;
import java.lang.Override;
import java.lang.String;

public final class RelationImpl<UnaryType extends LMObject, EffectiveType> extends FeaturedObject implements Relation<UnaryType, EffectiveType> {
  private static final FeatureGetter<Relation<?, ?>> GET_MAP = new FeatureGetter.Builder<Relation<?, ?>>().add(isotropy.lmf.core.lang.Relation.Features.name, isotropy.lmf.core.lang.Relation::name).add(isotropy.lmf.core.lang.Relation.Features.immutable, isotropy.lmf.core.lang.Relation::immutable).add(isotropy.lmf.core.lang.Relation.Features.many, isotropy.lmf.core.lang.Relation::many).add(isotropy.lmf.core.lang.Relation.Features.mandatory, isotropy.lmf.core.lang.Relation::mandatory).add(isotropy.lmf.core.lang.Relation.Features.rawFeature, isotropy.lmf.core.lang.Relation::rawFeature).add(isotropy.lmf.core.lang.Relation.Features.reference, isotropy.lmf.core.lang.Relation::reference).add(isotropy.lmf.core.lang.Relation.Features.lazy, isotropy.lmf.core.lang.Relation::lazy).add(isotropy.lmf.core.lang.Relation.Features.contains, isotropy.lmf.core.lang.Relation::contains).build();

  private static final FeatureSetter<Relation<?, ?>> SET_MAP = new FeatureSetter.Builder<Relation<?, ?>>().build();

  private final String name;

  private final boolean immutable;

  private final boolean many;

  private final boolean mandatory;

  private final RawFeature<UnaryType, EffectiveType> rawFeature;

  private final Reference<UnaryType> reference;

  private final boolean lazy;

  private final boolean contains;

  public RelationImpl(final String name, final boolean immutable, final boolean many,
      final boolean mandatory, final RawFeature<UnaryType, EffectiveType> rawFeature,
      final Reference<UnaryType> reference, final boolean lazy, final boolean contains) {
    this.name = name;
    this.immutable = immutable;
    this.many = many;
    this.mandatory = mandatory;
    this.rawFeature = rawFeature;
    this.reference = reference;
    this.lazy = lazy;
    this.contains = contains;
    setContainer(reference, Features.reference);
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
  public Reference<UnaryType> reference() {
    return reference;
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
