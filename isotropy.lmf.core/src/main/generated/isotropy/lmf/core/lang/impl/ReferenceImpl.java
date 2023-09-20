package isotropy.lmf.core.lang.impl;

import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Concept;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCoreDefinition;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Reference;
import isotropy.lmf.core.util.BuildUtils;
import java.lang.Override;
import java.util.List;
import java.util.function.Supplier;

public final class ReferenceImpl<T extends LMObject> extends FeaturedObject implements Reference<T> {
  private static final FeatureGetter<Reference<?>> GET_MAP = new FeatureGetter.Builder<Reference<?>>().add(isotropy.lmf.core.lang.Reference.Features.group, isotropy.lmf.core.lang.Reference::group).add(isotropy.lmf.core.lang.Reference.Features.parameters, isotropy.lmf.core.lang.Reference::parameters).build();

  private static final FeatureSetter<Reference<?>> SET_MAP = new FeatureSetter.Builder<Reference<?>>().build();

  private final Supplier<Concept<T>> group;

  private final List<Supplier<Concept<?>>> parameters;

  public ReferenceImpl(final Supplier<Concept<T>> group,
      final List<Supplier<Concept<?>>> parameters) {
    this.group = group;
    this.parameters = List.copyOf(parameters);
  }

  @Override
  public Concept<T> group() {
    return group.get();
  }

  @Override
  public List<Concept<?>> parameters() {
    return BuildUtils.collectSuppliers(parameters);
  }

  @Override
  public Group<Reference<?>> lmGroup() {
    return LMCoreDefinition.Groups.REFERENCE;
  }

  @Override
  protected FeatureSetter<Reference<?>> setterMap() {
    return SET_MAP;
  }

  @Override
  protected FeatureGetter<Reference<?>> getterMap() {
    return GET_MAP;
  }
}
