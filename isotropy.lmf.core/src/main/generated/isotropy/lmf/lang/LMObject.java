package isotropy.lmf.lang;

import isotropy.lmf.core.model.IFeaturedObject;

public interface LMObject extends IFeaturedObject<LMObject>, IFeaturedObject<T> {
  interface Builder extends IFeaturedObject.Builder<LMObject>, IFeaturedObject.Builder<T> {
  }
}
