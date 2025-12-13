package org.logoce.lmf.model.lang.impl;

import org.logoce.lmf.model.api.model.ModelNotifier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Primitive;
import org.logoce.lmf.model.lang.Unit;

public final class UnitImpl<T> extends FeaturedObject<Unit.Features<?>> implements Unit<T> {
  private static final int FEATURE_COUNT = 5;
  private final ModelNotifier<Unit.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final String matcher;
  private final String defaultValue;
  private final Primitive primitive;
  private final String extractor;

  public UnitImpl(final String name, final String matcher, final String defaultValue,
      final Primitive primitive, final String extractor) {
    this.name = name;
    this.matcher = matcher;
    this.defaultValue = defaultValue;
    this.primitive = primitive;
    this.extractor = extractor;
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Unit.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public String matcher() {
    return matcher;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public Primitive primitive() {
    return primitive;
  }

  @Override
  public String extractor() {
    return extractor;
  }

  @Override
  public Group<Unit<?>> lmGroup() {
    return LMCoreModelDefinition.Groups.UNIT;
  }

  @Override
  protected FeatureSetter<Unit<?>> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Unit<?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Unit.FeatureIDs.NAME -> 0;
      case Unit.FeatureIDs.MATCHER -> 1;
      case Unit.FeatureIDs.DEFAULT_VALUE -> 2;
      case Unit.FeatureIDs.PRIMITIVE -> 3;
      case Unit.FeatureIDs.EXTRACTOR -> 4;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Unit<?>> GET_MAP = new FeatureGetter.Builder<Unit<?>>(FEATURE_COUNT, UnitImpl::featureIndexStatic).add(Unit.FeatureIDs.NAME, Unit::name).add(Unit.FeatureIDs.MATCHER, Unit::matcher).add(Unit.FeatureIDs.DEFAULT_VALUE, Unit::defaultValue).add(Unit.FeatureIDs.PRIMITIVE, Unit::primitive).add(Unit.FeatureIDs.EXTRACTOR, Unit::extractor).build();
    private static final FeatureSetter<Unit<?>> SET_MAP = new FeatureSetter.Builder<Unit<?>>(FEATURE_COUNT, UnitImpl::featureIndexStatic).build();
  }
}
