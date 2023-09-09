package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.core.model.FeatureSetter;
import isotropy.lmf.core.model.FeaturedObject;
import java.lang.Override;
import java.lang.String;
import java.util.List;

public final class EnumImpl<T> extends FeaturedObject implements Enum<T> {
  private static final FeatureGetter<Enum<?>> GET_MAP = new FeatureGetter.Builder<Enum<?>>().add(Features.name, Enum::name).add(Features.literals, Enum::literals).build();

  private static final FeatureSetter<Enum<?>> SET_MAP = new FeatureSetter.Builder<Enum<?>>().build();

  private final String name;

  private final List<String> literals;

  public EnumImpl(final String name, final List<String> literals) {
    this.name = name;
    this.literals = List.copyOf(literals);
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
    return LMCoreDefinition.Groups.ENUM;
  }

  @Override
  protected FeatureSetter<Enum<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Enum<?>> getterMap() {
    return GET_MAP;
  }
}
