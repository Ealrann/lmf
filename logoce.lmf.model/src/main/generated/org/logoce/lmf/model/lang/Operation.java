package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.OperationBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Operation extends Named {
  static Builder builder() {
    return new OperationBuilder();
  }

  String content();
  Type<?> returnType();
  List<GenericParameter> returnTypeParameters();
  List<OperationParameter> parameters();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int CONTENT = 357588989;
    int RETURN_TYPE = -1807540602;
    int RETURN_TYPE_PARAMETERS = -1420505840;
    int PARAMETERS = 1608955878;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, Operation> CONTENT = new AttributeBuilder<String, String, Listener<String>, Operation>().name("content").immutable(true).id(Operation.FeatureIDs.CONTENT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Relation<Type<?>, Type<?>, Listener<Type<?>>, Operation> RETURN_TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, Operation>().name("returnType").immutable(true).lazy(true).id(Operation.FeatureIDs.RETURN_TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Operation> RETURN_TYPE_PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Operation>().name("returnTypeParameters").immutable(true).many(true).contains(true).id(Operation.FeatureIDs.RETURN_TYPE_PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    Relation<OperationParameter, List<OperationParameter>, Listener<List<OperationParameter>>, Operation> PARAMETERS = new RelationBuilder<OperationParameter, List<OperationParameter>, Listener<List<OperationParameter>>, Operation>().name("parameters").immutable(true).many(true).contains(true).id(Operation.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.OPERATION_PARAMETER).build();
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
