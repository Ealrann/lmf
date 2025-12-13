package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.ModelNotifier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Alias;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class AliasImpl extends FeaturedObject<Alias.Features<?>> implements Alias {
  private static final int FEATURE_COUNT = 2;
  private final ModelNotifier<Alias.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final String value;

  public AliasImpl(final String name, final String value) {
    this.name = name;
    this.value = value;
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Alias.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Group<Alias> lmGroup() {
    return LMCoreModelDefinition.Groups.ALIAS;
  }

  @Override
  protected FeatureSetter<Alias> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Alias> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Alias.FeatureIDs.NAME -> 0;
      case Alias.FeatureIDs.VALUE -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Alias> GET_MAP = new FeatureGetter.Builder<Alias>(FEATURE_COUNT, AliasImpl::featureIndexStatic).add(Alias.FeatureIDs.NAME, Alias::name).add(Alias.FeatureIDs.VALUE, Alias::value).build();
    private static final FeatureSetter<Alias> SET_MAP = new FeatureSetter.Builder<Alias>(FEATURE_COUNT, AliasImpl::featureIndexStatic).build();
  }
}
