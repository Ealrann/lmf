package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.IncludeBuilder;

public interface Include<T extends LMObject> extends LMObject {
  static <T extends LMObject> Builder<T> builder() {
    return new IncludeBuilder<>();
  }

  Group<T> group();
  List<GenericParameter> parameters();

  interface Features extends LMObject.Features<Features> {
    RawFeature<Group<?>, Group<?>> group = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.INCLUDE.GROUP);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.INCLUDE.PARAMETERS);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Include<T>> {
    Builder<T> group(Supplier<Group<T>> group);
    Builder<T> addParameter(Supplier<GenericParameter> parameter);
  }
}
