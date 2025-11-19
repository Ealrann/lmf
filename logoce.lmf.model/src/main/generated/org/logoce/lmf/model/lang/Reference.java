package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.ReferenceBuilder;

public interface Reference<T extends LMObject> extends LMObject {
  static <T extends LMObject> Builder<T> builder() {
    return new ReferenceBuilder<>();
  }

  Concept<T> group();
  List<Concept<?>> parameters();

  interface Features extends LMObject.Features<Features> {
    RawFeature<Concept<?>, Concept<?>> group = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.REFERENCE.GROUP);
    RawFeature<Concept<?>, List<Concept<?>>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.REFERENCE.PARAMETERS);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Reference<T>> {
    Builder<T> group(Supplier<Concept<T>> group);
    Builder<T> addParameter(Supplier<Concept<?>> parameter);
  }
}
