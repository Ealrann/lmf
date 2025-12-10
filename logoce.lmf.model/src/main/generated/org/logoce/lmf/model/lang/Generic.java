package org.logoce.lmf.model.lang;

import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericBuilder;

public interface Generic<T> extends Concept<T>, Datatype<T> {
  static <T> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  GenericExtension extension();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Concept.FeatureIDs<T>, Datatype.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int EXTENSION = 1695230195;
  }

  interface Builder<T> extends IFeaturedObject.Builder<Generic<T>> {
    Builder<T> name(String name);
    Builder<T> extension(Supplier<GenericExtension> extension);
  }
}
