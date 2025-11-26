package org.logoce.lmf.model.lang;

import java.lang.String;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericBuilder;

public interface Generic<T> extends Concept<T>, Datatype<T> {
  static <T> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  GenericExtension extension();

  interface Features<T extends Features<T>> extends Concept.Features<T>, Datatype.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<GenericExtension, GenericExtension> extension = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC.EXTENSION);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Generic<T>> {
    Builder<T> name(String name);
    Builder<T> extension(Supplier<GenericExtension> extension);
  }
}
