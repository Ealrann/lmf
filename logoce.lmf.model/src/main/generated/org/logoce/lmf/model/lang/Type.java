package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Type<T> extends Named {
  @Override
  IModelNotifier<? extends Features<?>> notifier();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME);
  }
}
