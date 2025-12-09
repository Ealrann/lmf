package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.OperationParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface OperationParameter extends Named {
  static Builder builder() {
    return new OperationParameterBuilder();
  }

  Type<?> type();
  List<GenericParameter> parameters();

  interface RFeatures<T extends RFeatures<T>> extends Named.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> OperationParameter.Features.TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> OperationParameter.Features.PARAMETERS);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int TYPE = 302950153;
    int PARAMETERS = -525565319;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Relation<Type<?>, Type<?>> TYPE = new RelationBuilder<Type<?>, Type<?>>().name("type").immutable(true).mandatory(true).lazy(true).rawFeature(OperationParameter.RFeatures.type).id(OperationParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).rawFeature(OperationParameter.RFeatures.parameters).id(OperationParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?>> ALL = List.of(NAME, TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<OperationParameter> {
    Builder name(String name);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
