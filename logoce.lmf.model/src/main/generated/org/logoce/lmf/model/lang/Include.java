package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.IncludeBuilder;

public interface Include<T extends LMObject> extends LMObject {
  static <T extends LMObject> Builder<T> builder() {
    return new IncludeBuilder<>();
  }

  Group<T> group();
  List<GenericParameter> parameters();

  interface FeatureIDs<T extends FeatureIDs<T>> extends LMObject.FeatureIDs<T> {
    int GROUP = 450930500;
    int PARAMETERS = -221199291;
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Include<T>> {
    Builder<T> group(Supplier<Group<T>> group);
    Builder<T> addParameter(Supplier<GenericParameter> parameter);
    Builder<T> addParameters(List<GenericParameter> parameters);
  }
}
