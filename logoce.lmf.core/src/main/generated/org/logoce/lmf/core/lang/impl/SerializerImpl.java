package org.logoce.lmf.core.lang.impl;

import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.Serializer;

public final class SerializerImpl extends FeaturedObject<Serializer.Features<?>> implements Serializer {
  private static final int FEATURE_COUNT = 3;
  private final ModelNotifier<Serializer.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String defaultValue;
  private final String create;
  private final String convert;

  public SerializerImpl(final String defaultValue, final String create, final String convert) {
    this.defaultValue = defaultValue;
    this.create = create;
    this.convert = convert;
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Serializer.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public String create() {
    return create;
  }

  @Override
  public String convert() {
    return convert;
  }

  @Override
  public Group<Serializer> lmGroup() {
    return LMCoreModelDefinition.Groups.SERIALIZER;
  }

  @Override
  protected FeatureSetter<Serializer> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Serializer> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Serializer.FeatureIDs.DEFAULT_VALUE -> 0;
      case Serializer.FeatureIDs.CREATE -> 1;
      case Serializer.FeatureIDs.CONVERT -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Serializer> GET_MAP = new FeatureGetter.Builder<Serializer>(FEATURE_COUNT, SerializerImpl::featureIndexStatic).add(Serializer.FeatureIDs.DEFAULT_VALUE, Serializer::defaultValue).add(Serializer.FeatureIDs.CREATE, Serializer::create).add(Serializer.FeatureIDs.CONVERT, Serializer::convert).build();
    private static final FeatureSetter<Serializer> SET_MAP = new FeatureSetter.Builder<Serializer>(FEATURE_COUNT, SerializerImpl::featureIndexStatic).build();
  }
}
