package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.OperationParameterBuilder;

public interface OperationParameter extends Named {
  static Builder builder() {
    return new OperationParameterBuilder();
  }

  Type<?> type();
  List<GenericParameter> parameters();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Named.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int TYPE = 302950153;
    int PARAMETERS = -525565319;
  }

  interface Builder extends IFeaturedObject.Builder<OperationParameter> {
    Builder name(String name);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
