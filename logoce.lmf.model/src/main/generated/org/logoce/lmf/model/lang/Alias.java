package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AliasBuilder;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Alias extends Named {
  static Builder builder() {
    return new AliasBuilder();
  }

  String value();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int VALUE = -1357955170;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, Alias> VALUE = new AttributeBuilder<String, String, Listener<String>, Alias>().name("value").immutable(true).mandatory(true).id(Alias.FeatureIDs.VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, VALUE);
  }

  interface Builder extends IFeaturedObject.Builder<Alias> {
    Builder name(String name);
    Builder value(String value);
  }
}
