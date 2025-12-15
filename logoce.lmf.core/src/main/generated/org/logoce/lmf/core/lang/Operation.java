package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.OperationBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;

public interface Operation extends Named {
  static Builder builder() {
    return new OperationBuilder();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  String content();
  Type<?> returnType();
  List<GenericParameter> returnTypeParameters();
  List<OperationParameter> parameters();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int CONTENT = -1592922937;
    int RETURN_TYPE = 1399186812;
    int RETURN_TYPE_PARAMETERS = 1576786054;
    int PARAMETERS = 520715996;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, Features<?>> CONTENT = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("content").immutable(true).id(Operation.FeatureIDs.CONTENT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Relation<Type<?>, Type<?>, Listener<Type<?>>, Features<?>> RETURN_TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, Features<?>>().name("returnType").immutable(true).lazy(true).id(Operation.FeatureIDs.RETURN_TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>> RETURN_TYPE_PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>>().name("returnTypeParameters").immutable(true).many(true).contains(true).id(Operation.FeatureIDs.RETURN_TYPE_PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    Relation<OperationParameter, List<OperationParameter>, Listener<List<OperationParameter>>, Features<?>> PARAMETERS = new RelationBuilder<OperationParameter, List<OperationParameter>, Listener<List<OperationParameter>>, Features<?>>().name("parameters").immutable(true).many(true).contains(true).id(Operation.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.OPERATION_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, CONTENT, RETURN_TYPE, RETURN_TYPE_PARAMETERS, PARAMETERS);
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
