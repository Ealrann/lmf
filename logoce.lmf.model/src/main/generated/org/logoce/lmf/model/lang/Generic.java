package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Generic<T> extends Concept<T>, Datatype<T> {
  static <T> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  GenericExtension extension();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int EXTENSION = 1695230195;
  }

  interface Features<T extends Features<T>> extends Concept.Features<T>, Datatype.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Relation<GenericExtension, GenericExtension, Listener<GenericExtension>, Generic<?>> EXTENSION = new RelationBuilder<GenericExtension, GenericExtension, Listener<GenericExtension>, Generic<?>>().name("extension").immutable(true).contains(true).id(Generic.FeatureIDs.EXTENSION).concept(() -> LMCoreModelDefinition.Groups.GENERIC_EXTENSION).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, EXTENSION);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Generic<T>> {
    Builder<T> name(String name);
    Builder<T> extension(Supplier<GenericExtension> extension);
  }
}
