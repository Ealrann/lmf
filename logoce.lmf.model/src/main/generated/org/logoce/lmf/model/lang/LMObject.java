package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelNotifier;

public interface LMObject extends IFeaturedObject {
  @Override
  IModelNotifier<? extends Features<?>> notifier();

  interface FeatureIDs {
  }

  interface Features<T extends Features<T>> extends IFeaturedObject.Features<T> {
    List<Feature<?, ?, ?, ?>> ALL = List.of();
  }
}
