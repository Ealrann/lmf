package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;

public interface LMObject extends IFeaturedObject {
  @Override
  IModelNotifier<? extends Features<?>> notifier();

  interface FeatureIDs {
  }

  interface Features<T extends Features<T>> extends IFeaturedObject.Features<T> {
    List<Feature<?, ?, ?, ?>> ALL = List.of();
  }
}
