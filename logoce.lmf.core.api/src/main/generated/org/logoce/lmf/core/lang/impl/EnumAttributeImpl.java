package org.logoce.lmf.core.lang.impl;

import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.EnumAttribute;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.Unit;

public final class EnumAttributeImpl extends FeaturedObject<EnumAttribute.Features<?>> implements EnumAttribute {
  private static final int FEATURE_COUNT = 3;
  private final ModelNotifier<EnumAttribute.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final Unit<?> unit;
  private final String defaultValue;

  public EnumAttributeImpl(final String name, final Unit<?> unit, final String defaultValue) {
    this.name = name;
    this.unit = unit;
    this.defaultValue = defaultValue;
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<EnumAttribute.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Unit<?> unit() {
    return unit;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public Group<EnumAttribute> lmGroup() {
    return LMCoreModelDefinition.Groups.ENUM_ATTRIBUTE;
  }

  @Override
  protected FeatureSetter<EnumAttribute> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<EnumAttribute> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case EnumAttribute.FeatureIDs.NAME -> 0;
      case EnumAttribute.FeatureIDs.UNIT -> 1;
      case EnumAttribute.FeatureIDs.DEFAULT_VALUE -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<EnumAttribute> GET_MAP = new FeatureGetter.Builder<EnumAttribute>(FEATURE_COUNT, EnumAttributeImpl::featureIndexStatic).add(EnumAttribute.FeatureIDs.NAME, EnumAttribute::name).add(EnumAttribute.FeatureIDs.UNIT, EnumAttribute::unit).add(EnumAttribute.FeatureIDs.DEFAULT_VALUE, EnumAttribute::defaultValue).build();
    private static final FeatureSetter<EnumAttribute> SET_MAP = new FeatureSetter.Builder<EnumAttribute>(FEATURE_COUNT, EnumAttributeImpl::featureIndexStatic).build();
  }
}
