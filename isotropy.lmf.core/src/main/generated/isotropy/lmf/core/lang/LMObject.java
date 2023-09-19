package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.model.IFeaturedObject;
import org.logoce.notification.api.IFeatures;

public interface LMObject extends IFeaturedObject {
  interface Features<T extends Features<T>> extends IFeatures<T> {
  }
}
