package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.notification.api.IFeatures;

public interface LMObject extends IFeaturedObject {
  interface FeatureIDs {
  }

  interface Features<T extends Features<T>> extends IFeatures<T> {
    List<Feature<?, ?, ?, ?>> ALL = List.of();
  }
}
