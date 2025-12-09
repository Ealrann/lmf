package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface GenericParameter extends LMObject {
  static Builder builder() {
    return new GenericParameterBuilder();
  }

  boolean wildcard();
  BoundType wildcardBoundType();
  Type<?> type();
  List<GenericParameter> parameters();

  interface RFeatures<T extends RFeatures<T>> extends LMObject.RFeatures<T> {
    RawFeature<Boolean, Boolean> wildcard = new RawFeature<>(false,false,() -> GenericParameter.Features.WILDCARD);
    RawFeature<BoundType, BoundType> wildcardBoundType = new RawFeature<>(false,false,() -> GenericParameter.Features.WILDCARD_BOUND_TYPE);
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> GenericParameter.Features.TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> GenericParameter.Features.PARAMETERS);
  }

  interface FeatureIDs {
    int WILDCARD = 520310873;
    int WILDCARD_BOUND_TYPE = 673018239;
    int TYPE = -1625571015;
    int PARAMETERS = -47630167;
  }

  interface Features {
    Attribute<Boolean, Boolean> WILDCARD = new AttributeBuilder<Boolean, Boolean>().name("wildcard").immutable(true).rawFeature(GenericParameter.RFeatures.wildcard).id(GenericParameter.FeatureIDs.WILDCARD).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<BoundType, BoundType> WILDCARD_BOUND_TYPE = new AttributeBuilder<BoundType, BoundType>().name("wildcardBoundType").immutable(true).rawFeature(GenericParameter.RFeatures.wildcardBoundType).id(GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
    Relation<Type<?>, Type<?>> TYPE = new RelationBuilder<Type<?>, Type<?>>().name("type").immutable(true).mandatory(true).lazy(true).rawFeature(GenericParameter.RFeatures.type).id(GenericParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).rawFeature(GenericParameter.RFeatures.parameters).id(GenericParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?>> ALL = List.of(WILDCARD, WILDCARD_BOUND_TYPE, TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericParameter> {
    Builder wildcard(boolean wildcard);
    Builder wildcardBoundType(BoundType wildcardBoundType);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
