package org.logoce.lmf.model.lang;

import java.lang.Boolean;
import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;

public interface GenericParameter extends LMObject {
  static Builder builder() {
    return new GenericParameterBuilder();
  }

  boolean wildcard();
  BoundType wildcardBoundType();
  Type<?> type();
  List<GenericParameter> parameters();

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    RawFeature<Boolean, Boolean> wildcard = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.WILDCARD);
    RawFeature<BoundType, BoundType> wildcardBoundType = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.WILDCARD_BOUND_TYPE);
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericParameter> {
    Builder wildcard(boolean wildcard);
    Builder wildcardBoundType(BoundType wildcardBoundType);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
  }
}
