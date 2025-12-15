package org.logoce.lmf.core.lang.impl;

import java.util.List;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.feature.FeatureGetter;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;

public final class EnumImpl<T> extends FeaturedObject<Enum.Features<?>> implements Enum<T> {
  private static final int FEATURE_COUNT = 2;
  private final ModelNotifier<Enum.Features<?>> notifier = new ModelNotifier<>(this, FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final List<String> literals;

  public EnumImpl(final String name, final List<String> literals) {
    this.name = name;
    this.literals = List.copyOf(literals);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Enum.Features<?>> notifier() {
    return notifier;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<String> literals() {
    return literals;
  }

  @Override
  public Group<Enum<?>> lmGroup() {
    return LMCoreModelDefinition.Groups.ENUM;
  }

  @Override
  protected FeatureSetter<Enum<?>> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Enum<?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Enum.FeatureIDs.NAME -> 0;
      case Enum.FeatureIDs.LITERALS -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Enum<?>> GET_MAP = new FeatureGetter.Builder<Enum<?>>(FEATURE_COUNT, EnumImpl::featureIndexStatic).add(Enum.FeatureIDs.NAME, Enum::name).add(Enum.FeatureIDs.LITERALS, Enum::literals).build();
    private static final FeatureSetter<Enum<?>> SET_MAP = new FeatureSetter.Builder<Enum<?>>(FEATURE_COUNT, EnumImpl::featureIndexStatic).build();
  }
}
