package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.BoundType;
import org.logoce.lmf.model.lang.GenericExtension;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;
import org.logoce.lmf.model.lang.Type;

public final class GenericExtensionImpl extends FeaturedObject implements GenericExtension {
  private static final FeatureGetter<GenericExtension> GET_MAP = new FeatureGetter.Builder<GenericExtension>().add(GenericExtension.RFeatures.type, GenericExtension::type).add(GenericExtension.RFeatures.boundType, GenericExtension::boundType).add(GenericExtension.RFeatures.parameters, GenericExtension::parameters).build();
  private static final FeatureSetter<GenericExtension> SET_MAP = new FeatureSetter.Builder<GenericExtension>().build();
  private final Supplier<Type<?>> type;
  private final BoundType boundType;
  private final List<GenericParameter> parameters;

  public GenericExtensionImpl(final Supplier<Type<?>> type, final BoundType boundType,
      final List<GenericParameter> parameters) {
    this.type = type;
    this.boundType = boundType;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, GenericExtension.RFeatures.parameters);
    eDeliver(true);
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
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<GenericExtension> getterMap() {
    return GET_MAP;
  }

  @Override
  protected int featureIndex(int featureId) {
    return switch (featureId) {
      case GenericExtension.FeatureIDs.TYPE -> 0;
      case GenericExtension.FeatureIDs.BOUND_TYPE -> 1;
      case GenericExtension.FeatureIDs.PARAMETERS -> 2;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
