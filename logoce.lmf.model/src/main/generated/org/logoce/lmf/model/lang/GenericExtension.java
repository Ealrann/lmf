package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface GenericExtension extends LMObject {
  static Builder builder() {
    return new GenericExtensionBuilder();
  }

  Type<?> type();
  BoundType boundType();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int TYPE = 1591035491;
    int BOUND_TYPE = 1549707343;
    int PARAMETERS = -1733249453;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Relation<Type<?>, Type<?>, Listener<Type<?>>, GenericExtension> TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, GenericExtension>().name("type").immutable(true).lazy(true).id(GenericExtension.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Attribute<BoundType, BoundType, Listener<BoundType>, GenericExtension> BOUND_TYPE = new AttributeBuilder<BoundType, BoundType, Listener<BoundType>, GenericExtension>().name("boundType").immutable(true).id(GenericExtension.FeatureIDs.BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, GenericExtension> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, GenericExtension>().name("parameters").immutable(true).many(true).contains(true).id(GenericExtension.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(TYPE, BOUND_TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericExtension> {
    Builder type(Supplier<Type<?>> type);
    Builder boundType(BoundType boundType);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
