package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.lang.builder.GenericBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;
import org.logoce.lmf.core.notification.listener.Listener;

public interface Generic<T> extends Concept<T>, Datatype<T> {
  static <T> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  GenericExtension extension();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int EXTENSION = 1695230195;
  }

  interface Features<T extends Features<T>> extends Concept.Features<T>, Datatype.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Relation<GenericExtension, GenericExtension, Listener<GenericExtension>, Features<?>> EXTENSION = new RelationBuilder<GenericExtension, GenericExtension, Listener<GenericExtension>, Features<?>>().name("extension").immutable(true).contains(true).id(Generic.FeatureIDs.EXTENSION).concept(() -> LMCoreModelDefinition.Groups.GENERIC_EXTENSION).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, EXTENSION);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Generic<T>> {
    Builder<T> name(String name);
    Builder<T> extension(Supplier<GenericExtension> extension);
  }
}
