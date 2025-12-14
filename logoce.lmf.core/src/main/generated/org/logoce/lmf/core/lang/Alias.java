package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.lang.builder.AliasBuilder;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.notification.listener.Listener;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  String value();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int VALUE = -1357955170;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, Features<?>> VALUE = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("value").immutable(true).mandatory(true).id(Alias.FeatureIDs.VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, VALUE);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);
    Builder value(String value);
  }
}
