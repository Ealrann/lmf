package org.logoce.lmf.model.lang;

import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;

public interface GenericExtension extends LMObject {
  static Builder builder() {
    return new GenericExtensionBuilder();
  }

  LMEntity<?> type();
  BoundType boundType();
  GenericParameter parameter();

  interface Features extends LMObject.Features<Features> {
    RawFeature<LMEntity<?>, LMEntity<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC_EXTENSION.TYPE);
    RawFeature<BoundType, BoundType> boundType = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC_EXTENSION.BOUND_TYPE);
    RawFeature<GenericParameter, GenericParameter> parameter = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC_EXTENSION.PARAMETER);
  }

  interface Builder extends IFeaturedObject.Builder<GenericExtension> {
    Builder type(Supplier<LMEntity<?>> type);
    Builder boundType(BoundType boundType);
    Builder parameter(Supplier<GenericParameter> parameter);
  }
}
