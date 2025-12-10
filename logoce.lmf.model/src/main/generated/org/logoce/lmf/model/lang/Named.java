package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Named extends LMObject {
  String name();

  interface FeatureIDs {
    int NAME = 1328200565;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = new AttributeBuilder<String, String, Listener<String>, Named>().name("name").immutable(true).mandatory(true).id(Named.FeatureIDs.NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME);
  }
}
