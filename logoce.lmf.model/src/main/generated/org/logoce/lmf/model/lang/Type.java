package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Type<T> extends Named {
  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME);
  }
}
