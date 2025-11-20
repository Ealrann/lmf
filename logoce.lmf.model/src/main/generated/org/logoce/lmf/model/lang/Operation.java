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
  Type<?> type();
  List<OperationParameter> parameters();

  interface Features extends Named.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> content = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.OPERATION.CONTENT);
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.OPERATION.TYPE);
    RawFeature<OperationParameter, List<OperationParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.OPERATION.PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<Operation> {
    Builder name(String name);
    Builder content(String content);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<OperationParameter> parameter);
  }
}
