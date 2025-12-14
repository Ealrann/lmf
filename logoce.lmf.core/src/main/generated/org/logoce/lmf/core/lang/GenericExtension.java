package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;
import org.logoce.lmf.core.notification.listener.Listener;

public interface GenericExtension extends LMObject {
  static Builder builder() {
    return new GenericExtensionBuilder();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  Type<?> type();
  BoundType boundType();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int TYPE = 1591035491;
    int BOUND_TYPE = 1549707343;
    int PARAMETERS = -1733249453;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Relation<Type<?>, Type<?>, Listener<Type<?>>, Features<?>> TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, Features<?>>().name("type").immutable(true).lazy(true).id(GenericExtension.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Attribute<BoundType, BoundType, Listener<BoundType>, Features<?>> BOUND_TYPE = new AttributeBuilder<BoundType, BoundType, Listener<BoundType>, Features<?>>().name("boundType").immutable(true).id(GenericExtension.FeatureIDs.BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>>().name("parameters").immutable(true).many(true).contains(true).id(GenericExtension.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(TYPE, BOUND_TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericExtension> {
    Builder type(Supplier<Type<?>> type);
    Builder boundType(BoundType boundType);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
