package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
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

  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> content = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.OPERATION.CONTENT);
    RawFeature<Type<?>, Type<?>> returnType = new RawFeature<>(false,true,() -> LMCoreModelDefinition.Features.OPERATION.RETURN_TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> returnTypeParameters = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.OPERATION.RETURN_TYPE_PARAMETERS);
    RawFeature<OperationParameter, List<OperationParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.OPERATION.PARAMETERS);
  }

  interface FeatureIDs {
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
