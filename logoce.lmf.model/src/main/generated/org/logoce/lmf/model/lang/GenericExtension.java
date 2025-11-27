package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;

public interface GenericExtension extends LMObject {
  static Builder builder() {
    return new GenericExtensionBuilder();
  }

  Type<?> type();
  BoundType boundType();
  List<GenericParameter> parameters();

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC_EXTENSION.TYPE);
    RawFeature<BoundType, BoundType> boundType = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC_EXTENSION.BOUND_TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.GENERIC_EXTENSION.PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericExtension> {
    Builder type(Supplier<Type<?>> type);
    Builder boundType(BoundType boundType);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
