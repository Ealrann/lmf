package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.OperationBuilder;

public interface Operation extends Named {
  static Builder builder() {
    return new OperationBuilder();
  }

  String content();
  Type<?> returnType();
  List<GenericParameter> returnTypeParameters();
  List<OperationParameter> parameters();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Named.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int CONTENT = 357588989;
    int RETURN_TYPE = -1807540602;
    int RETURN_TYPE_PARAMETERS = -1420505840;
    int PARAMETERS = 1608955878;
  }

  interface Builder extends IFeaturedObject.Builder<Operation> {
    Builder name(String name);
    Builder content(String content);
    Builder returnType(Supplier<Type<?>> returnType);
    Builder addReturnTypeParameter(Supplier<GenericParameter> returnTypeParameter);
    Builder addParameter(Supplier<OperationParameter> parameter);
    Builder addReturnTypeParameters(List<GenericParameter> returnTypeParameters);
    Builder addParameters(List<OperationParameter> parameters);
  }
}
