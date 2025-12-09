package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.notification.api.IFeatures;

public interface LMObject extends IFeaturedObject {
  interface RFeatures<T extends RFeatures<T>> extends IFeatures<T> {
  }

  interface FeatureIDs {
  }

  interface Features {
    List<Feature<?, ?>> ALL = List.of();
  }
}
