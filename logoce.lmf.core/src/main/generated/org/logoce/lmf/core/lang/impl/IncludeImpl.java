package org.logoce.lmf.core.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.GenericParameter;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Include;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMObject;

public final class IncludeImpl<T extends LMObject> extends FeaturedObject<Include.Features<?>> implements Include<T> {
  private static final int FEATURE_COUNT = 2;
  private final ModelNotifier<Include.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final Supplier<Group<T>> group;
  private final List<GenericParameter> parameters;

  public IncludeImpl(final Supplier<Group<T>> group, final List<GenericParameter> parameters) {
    this.group = group;
    this.parameters = List.copyOf(parameters);
    setContainer(parameters, Include.FeatureIDs.PARAMETERS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Include.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public Group<T> group() {
    return group.get();
  }

  @Override
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public Group<Include<?>> lmGroup() {
    return LMCoreModelDefinition.Groups.INCLUDE;
  }

  @Override
  protected FeatureSetter<Include<?>> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Include<?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Include.FeatureIDs.GROUP -> 0;
      case Include.FeatureIDs.PARAMETERS -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Include<?>> GET_MAP = new FeatureGetter.Builder<Include<?>>(FEATURE_COUNT, IncludeImpl::featureIndexStatic).add(Include.FeatureIDs.GROUP, Include::group).add(Include.FeatureIDs.PARAMETERS, Include::parameters).build();
    private static final FeatureSetter<Include<?>> SET_MAP = new FeatureSetter.Builder<Include<?>>(FEATURE_COUNT, IncludeImpl::featureIndexStatic).build();
  }
}
