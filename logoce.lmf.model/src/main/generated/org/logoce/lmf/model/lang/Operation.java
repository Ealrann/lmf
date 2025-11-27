package org.logoce.lmf.model.lang;

import java.lang.String;
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
    RawFeature<String, String> content = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.OPERATION.CONTENT);
    RawFeature<Type<?>, Type<?>> returnType = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.OPERATION.RETURN_TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> returnTypeParameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.OPERATION.RETURN_TYPE_PARAMETERS);
    RawFeature<OperationParameter, List<OperationParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.OPERATION.PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<Operation> {
    Builder name(String name);
    Builder content(String content);
    Builder returnType(Supplier<Type<?>> returnType);
    Builder addReturnTypeParameter(Supplier<GenericParameter> returnTypeParameter);
    Builder addParameter(Supplier<OperationParameter> parameter);
    Builder returnTypeParameters(List<GenericParameter> returnTypeParameters);
    Builder parameters(List<OperationParameter> parameters);
  }
}
