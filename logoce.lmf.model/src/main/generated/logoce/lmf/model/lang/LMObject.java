package logoce.lmf.model.lang;

import logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.notification.api.IFeatures;

public interface LMObject extends IFeaturedObject {
  interface Features<T extends Features<T>> extends IFeatures<T> {
  }
}
