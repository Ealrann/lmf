package org.logoce.lmf.model.lang;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.notification.api.IFeatures;

public interface LMObject extends IFeaturedObject {
  interface FeatureIDs<T extends FeatureIDs<T>> extends IFeatures<T> {
  }
}
