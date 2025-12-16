package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;

public interface Named extends LMObject {
  @Override
  IModelNotifier<? extends Features<?>> notifier();
  String name();

  interface FeatureIDs {
    int NAME = -2087346709;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Attribute<String, String, Listener<String>, Features<?>> NAME = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("name").immutable(true).mandatory(true).id(Named.FeatureIDs.NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME);
  }
}
