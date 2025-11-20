package org.logoce.lmf.model.lang;

import java.lang.String;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.OperationParameterBuilder;

public interface OperationParameter extends Named {
  static Builder builder() {
    return new OperationParameterBuilder();
  }

  Type<?> type();

  interface Features extends Named.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.OPERATION_PARAMETER.TYPE);
  }

  interface Builder extends IFeaturedObject.Builder<OperationParameter> {
    Builder name(String name);
    Builder type(Supplier<Type<?>> type);
  }
}
