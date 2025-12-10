package org.logoce.lmf.model.lang;

import java.util.List;

public interface Concept<T> extends Type<T> {
  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
  }

  interface Features<T extends Features<T>> extends Type.Features<T> {
    Attribute<String, String> NAME = Named.Features.NAME;
    List<Feature<?, ?>> ALL = List.of(NAME);
  }
}
