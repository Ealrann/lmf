package org.logoce.lmf.model.lang;

import java.lang.Boolean;
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
  LMEntity<?> type();
  GenericParameter parameter();

  interface Features extends LMObject.Features<Features> {
    RawFeature<Boolean, Boolean> wildcard = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.WILDCARD);
    RawFeature<BoundType, BoundType> wildcardBoundType = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.WILDCARD_BOUND_TYPE);
    RawFeature<LMEntity<?>, LMEntity<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.TYPE);
    RawFeature<GenericParameter, GenericParameter> parameter = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC_PARAMETER.PARAMETER);
  }

  interface Builder extends IFeaturedObject.Builder<GenericParameter> {
    Builder wildcard(boolean wildcard);
    Builder wildcardBoundType(BoundType wildcardBoundType);
    Builder type(Supplier<LMEntity<?>> type);
    Builder parameter(Supplier<GenericParameter> parameter);
  }
}
