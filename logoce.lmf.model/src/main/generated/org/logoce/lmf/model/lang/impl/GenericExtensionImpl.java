package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.ModelNotifier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Type;

public final class GenericExtensionImpl extends FeaturedObject<GenericExtension.Features<?>> implements GenericExtension {
  private static final int FEATURE_COUNT = 3;
  private final ModelNotifier<GenericExtension.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final Supplier<Type<?>> type;
  private final BoundType boundType;
  private final List<GenericParameter> parameters;

  public GenericExtensionImpl(final Supplier<Type<?>> type, final BoundType boundType,
      final List<GenericParameter> parameters) {
    this.type = type;
    this.boundType = boundType;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, GenericExtension.FeatureIDs.PARAMETERS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<GenericExtension.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public Type<?> type() {
    return type.get();
  }

  @Override
  public BoundType boundType() {
    return boundType;
  }

  @Override
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<GenericExtension> lmGroup() {
    return LMCoreModelDefinition.Groups.GENERIC_EXTENSION;
  }

  @Override
  protected FeatureSetter<GenericExtension> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<GenericExtension> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case GenericExtension.FeatureIDs.TYPE -> 0;
      case GenericExtension.FeatureIDs.BOUND_TYPE -> 1;
      case GenericExtension.FeatureIDs.PARAMETERS -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<GenericExtension> GET_MAP = new FeatureGetter.Builder<GenericExtension>(FEATURE_COUNT, GenericExtensionImpl::featureIndexStatic).add(GenericExtension.FeatureIDs.TYPE, GenericExtension::type).add(GenericExtension.FeatureIDs.BOUND_TYPE, GenericExtension::boundType).add(GenericExtension.FeatureIDs.PARAMETERS, GenericExtension::parameters).build();
    private static final FeatureSetter<GenericExtension> SET_MAP = new FeatureSetter.Builder<GenericExtension>(FEATURE_COUNT, GenericExtensionImpl::featureIndexStatic).build();
  }
}
