package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.OperationBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface Operation extends Named {
  static Builder builder() {
    return new OperationBuilder();
  }

  String content();
  Type<?> returnType();
  List<GenericParameter> returnTypeParameters();
  List<OperationParameter> parameters();

  interface RFeatures<T extends RFeatures<T>> extends Named.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, String> content = new RawFeature<>(false,false,() -> Operation.Features.CONTENT);
    RawFeature<Type<?>, Type<?>> returnType = new RawFeature<>(false,true,() -> Operation.Features.RETURN_TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> returnTypeParameters = new RawFeature<>(true,true,() -> Operation.Features.RETURN_TYPE_PARAMETERS);
    RawFeature<OperationParameter, List<OperationParameter>> parameters = new RawFeature<>(true,true,() -> Operation.Features.PARAMETERS);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int CONTENT = 357588989;
    int RETURN_TYPE = -1807540602;
    int RETURN_TYPE_PARAMETERS = -1420505840;
    int PARAMETERS = 1608955878;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> CONTENT = new AttributeBuilder<String, String>().name("content").immutable(true).rawFeature(Operation.RFeatures.content).id(Operation.FeatureIDs.CONTENT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Relation<Type<?>, Type<?>> RETURN_TYPE = new RelationBuilder<Type<?>, Type<?>>().name("returnType").immutable(true).lazy(true).rawFeature(Operation.RFeatures.returnType).id(Operation.FeatureIDs.RETURN_TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>> RETURN_TYPE_PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("returnTypeParameters").immutable(true).many(true).contains(true).rawFeature(Operation.RFeatures.returnTypeParameters).id(Operation.FeatureIDs.RETURN_TYPE_PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    Relation<OperationParameter, List<OperationParameter>> PARAMETERS = new RelationBuilder<OperationParameter, List<OperationParameter>>().name("parameters").immutable(true).many(true).contains(true).rawFeature(Operation.RFeatures.parameters).id(Operation.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.OPERATION_PARAMETER).build();
    List<Feature<?, ?>> ALL = List.of(NAME, CONTENT, RETURN_TYPE, RETURN_TYPE_PARAMETERS, PARAMETERS);
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
