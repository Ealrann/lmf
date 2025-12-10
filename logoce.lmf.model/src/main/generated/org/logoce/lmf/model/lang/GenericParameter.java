package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;

public interface GenericParameter extends LMObject {
  static Builder builder() {
    return new GenericParameterBuilder();
  }

  boolean wildcard();
  BoundType wildcardBoundType();
  Type<?> type();
  List<GenericParameter> parameters();

  interface FeatureIDs<T extends FeatureIDs<T>> extends LMObject.FeatureIDs<T> {
    int WILDCARD = 520310873;
    int WILDCARD_BOUND_TYPE = 673018239;
    int TYPE = -1625571015;
    int PARAMETERS = -47630167;
  }

  interface Builder extends IFeaturedObject.Builder<GenericParameter> {
    Builder wildcard(boolean wildcard);
    Builder wildcardBoundType(BoundType wildcardBoundType);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
