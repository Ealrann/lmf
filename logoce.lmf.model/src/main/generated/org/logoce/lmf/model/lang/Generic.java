package org.logoce.lmf.model.lang;

import java.lang.String;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericBuilder;

public interface Generic<T extends LMEntity<?>> extends Concept<T> {
  static <T extends LMEntity<?>> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  GenericExtension extension();

  interface Features extends Concept.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<GenericExtension, GenericExtension> extension = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC.EXTENSION);
  }

  interface Builder<T extends LMEntity<?>> extends IFeaturedObject.Builder<Generic<T>> {
    Builder<T> name(String name);
    Builder<T> extension(Supplier<GenericExtension> extension);
  }
}
