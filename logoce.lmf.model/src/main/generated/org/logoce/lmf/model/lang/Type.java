package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;

public interface Type<T> extends Named {
  interface RFeatures<T extends RFeatures<T>> extends Named.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    List<Feature<?, ?>> ALL = List.of(NAME);
  }
}
