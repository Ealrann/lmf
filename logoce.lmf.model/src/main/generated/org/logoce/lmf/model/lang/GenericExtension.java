package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface GenericExtension extends LMObject {
  static Builder builder() {
    return new GenericExtensionBuilder();
  }

  Type<?> type();
  BoundType boundType();
  List<GenericParameter> parameters();

  interface RFeatures<T extends RFeatures<T>> extends LMObject.RFeatures<T> {
    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> GenericExtension.Features.TYPE);
    RawFeature<BoundType, BoundType> boundType = new RawFeature<>(false,false,() -> GenericExtension.Features.BOUND_TYPE);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> GenericExtension.Features.PARAMETERS);
  }

  interface FeatureIDs {
    int TYPE = 1591035491;
    int BOUND_TYPE = 1549707343;
    int PARAMETERS = -1733249453;
  }

  interface Features {
    Relation<Type<?>, Type<?>> TYPE = new RelationBuilder<Type<?>, Type<?>>().name("type").immutable(true).lazy(true).rawFeature(GenericExtension.RFeatures.type).id(GenericExtension.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Attribute<BoundType, BoundType> BOUND_TYPE = new AttributeBuilder<BoundType, BoundType>().name("boundType").immutable(true).rawFeature(GenericExtension.RFeatures.boundType).id(GenericExtension.FeatureIDs.BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).rawFeature(GenericExtension.RFeatures.parameters).id(GenericExtension.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?>> ALL = List.of(TYPE, BOUND_TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericExtension> {
    Builder type(Supplier<Type<?>> type);
    Builder boundType(BoundType boundType);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
