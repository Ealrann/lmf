package org.logoce.lmf.model.lang;

import org.logoce.lmf.model.api.feature.RawFeature;

public interface Type<T> extends Named {
  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
  }
}
