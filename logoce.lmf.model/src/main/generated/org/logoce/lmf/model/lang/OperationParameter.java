package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.OperationParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface OperationParameter extends Named {
  static Builder builder() {
    return new OperationParameterBuilder();
  }

  Type<?> type();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int TYPE = 302950153;
    int PARAMETERS = -525565319;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Relation<Type<?>, Type<?>, Listener<Type<?>>, Features<?>> TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, Features<?>>().name("type").immutable(true).mandatory(true).lazy(true).id(OperationParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>>().name("parameters").immutable(true).many(true).contains(true).id(OperationParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<OperationParameter> {
    Builder name(String name);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
