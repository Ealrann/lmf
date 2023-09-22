package logoce.lmf.model.lang.impl;

import java.lang.Override;
import java.lang.String;
import java.util.List;
import logoce.lmf.model.api.model.FeaturedObject;
import logoce.lmf.model.feature.FeatureGetter;
import logoce.lmf.model.feature.FeatureSetter;
import logoce.lmf.model.lang.Alias;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.LMCoreDefinition;

public final class AliasImpl extends FeaturedObject implements Alias {
  private static final FeatureGetter<Alias> GET_MAP = new FeatureGetter.Builder<Alias>().add(logoce.lmf.model.lang.Alias.Features.name, logoce.lmf.model.lang.Alias::name).add(logoce.lmf.model.lang.Alias.Features.words, logoce.lmf.model.lang.Alias::words).build();

  private static final FeatureSetter<Alias> SET_MAP = new FeatureSetter.Builder<Alias>().build();

  private final String name;

  private final List<String> words;

  public AliasImpl(final String name, final List<String> words) {
    this.name = name;
    this.words = List.copyOf(words);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<String> words() {
    return words;
  }

  @Override
  public Group<Alias> lmGroup() {
    return LMCoreDefinition.Groups.ALIAS;
  }

  @Override
  protected FeatureSetter<Alias> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Alias> getterMap() {
    return GET_MAP;
  }
}
