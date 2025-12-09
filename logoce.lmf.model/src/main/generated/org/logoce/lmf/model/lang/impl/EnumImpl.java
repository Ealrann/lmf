package org.logoce.lmf.model.lang.impl;

import java.util.List;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class EnumImpl<T> extends FeaturedObject implements Enum<T> {
  private static final FeatureGetter<Enum<?>> GET_MAP = new FeatureGetter.Builder<Enum<?>>().add(Enum.RFeatures.name, Enum::name).add(Enum.RFeatures.literals, Enum::literals).build();
  private static final FeatureSetter<Enum<?>> SET_MAP = new FeatureSetter.Builder<Enum<?>>().build();
  private final String name;
  private final List<String> literals;

  public EnumImpl(final String name, final List<String> literals) {
    this.name = name;
    this.literals = List.copyOf(literals);
    eDeliver(true);
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
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Enum<?>> getterMap() {
    return GET_MAP;
  }

  @Override
  protected int featureIndex(int featureId) {
    return switch (featureId) {
      case Enum.FeatureIDs.NAME -> 0;
      case Enum.FeatureIDs.LITERALS -> 1;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }
}
