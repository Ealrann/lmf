package org.logoce.lmf.model.lang.impl;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.api.model.ModelNotifier;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Datatype;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.GenericParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCoreModelDefinition;

public final class AttributeImpl<UnaryType, EffectiveType, ListenerType, ParentGroup> extends FeaturedObject<Attribute.Features<?>> implements Attribute<UnaryType, EffectiveType, ListenerType, ParentGroup> {
  private static final int FEATURE_COUNT = 8;
  private final ModelNotifier<Attribute.Features<?>> notifier = new ModelNotifier<>(FEATURE_COUNT, this::featureIndex);
  private final String name;
  private final boolean immutable;
  private final int id;
  private final boolean many;
  private final boolean mandatory;
  private final List<GenericParameter> parameters;
  private final Supplier<Datatype<UnaryType>> datatype;
  private final String defaultValue;

  public AttributeImpl(final String name, final boolean immutable, final int id, final boolean many,
      final boolean mandatory, final List<GenericParameter> parameters,
      final Supplier<Datatype<UnaryType>> datatype, final String defaultValue) {
    this.name = name;
    this.immutable = immutable;
    this.id = id;
    this.many = many;
    this.mandatory = mandatory;
    this.parameters = List.copyOf(parameters);
    this.datatype = datatype;
    this.defaultValue = defaultValue;
    setContainer(parameters, Feature.FeatureIDs.PARAMETERS);
    notifier.eDeliver(true);
  }

  @Override
  public IModelNotifier.Impl<Attribute.Features<?>> notifier() {
    return notifier;
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
  public int id() {
    return id;
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
  public List<GenericParameter> parameters() {
    return parameters;
  }

  @Override
  public Datatype<UnaryType> datatype() {
    return datatype.get();
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

  @Override
  public Group<Attribute<?, ?, ?, ?>> lmGroup() {
    return LMCoreModelDefinition.Groups.ATTRIBUTE;
  }

  @Override
  protected FeatureSetter<Attribute<?, ?, ?, ?>> setterMap() {
    return Inserters.SET_MAP;
  }

  @Override
  protected FeatureGetter<Attribute<?, ?, ?, ?>> getterMap() {
    return Inserters.GET_MAP;
  }

  public static int featureIndexStatic(int featureId) {
    return switch (featureId) {
      case Attribute.FeatureIDs.NAME -> 0;
      case Attribute.FeatureIDs.IMMUTABLE -> 1;
      case Attribute.FeatureIDs.ID -> 2;
      case Attribute.FeatureIDs.MANY -> 3;
      case Attribute.FeatureIDs.MANDATORY -> 4;
      case Attribute.FeatureIDs.PARAMETERS -> 5;
      case Attribute.FeatureIDs.DATATYPE -> 6;
      case Attribute.FeatureIDs.DEFAULT_VALUE -> 7;
      default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
    };
  }

  @Override
  public int featureIndex(int featureId) {
    return featureIndexStatic(featureId);
  }

  private static final class Inserters {
    private static final FeatureGetter<Attribute<?, ?, ?, ?>> GET_MAP = new FeatureGetter.Builder<Attribute<?, ?, ?, ?>>(FEATURE_COUNT, AttributeImpl::featureIndexStatic).add(Attribute.FeatureIDs.NAME, Attribute::name).add(Attribute.FeatureIDs.IMMUTABLE, Attribute::immutable).add(Attribute.FeatureIDs.ID, Attribute::id).add(Attribute.FeatureIDs.MANY, Attribute::many).add(Attribute.FeatureIDs.MANDATORY, Attribute::mandatory).add(Attribute.FeatureIDs.PARAMETERS, Attribute::parameters).add(Attribute.FeatureIDs.DATATYPE, Attribute::datatype).add(Attribute.FeatureIDs.DEFAULT_VALUE, Attribute::defaultValue).build();
    private static final FeatureSetter<Attribute<?, ?, ?, ?>> SET_MAP = new FeatureSetter.Builder<Attribute<?, ?, ?, ?>>(FEATURE_COUNT, AttributeImpl::featureIndexStatic).build();
  }
}
