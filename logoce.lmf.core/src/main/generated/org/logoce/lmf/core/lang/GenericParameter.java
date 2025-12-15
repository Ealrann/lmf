package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.BooleanListener;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;

public interface GenericParameter extends LMObject {
  static Builder builder() {
    return new GenericParameterBuilder();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  boolean wildcard();
  BoundType wildcardBoundType();
  Type<?> type();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int WILDCARD = 141849379;
    int WILDCARD_BOUND_TYPE = -792342027;
    int TYPE = -1001268989;
    int PARAMETERS = 1323094259;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> WILDCARD = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("wildcard").immutable(true).id(GenericParameter.FeatureIDs.WILDCARD).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<BoundType, BoundType, Listener<BoundType>, Features<?>> WILDCARD_BOUND_TYPE = new AttributeBuilder<BoundType, BoundType, Listener<BoundType>, Features<?>>().name("wildcardBoundType").immutable(true).id(GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
    Relation<Type<?>, Type<?>, Listener<Type<?>>, Features<?>> TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, Features<?>>().name("type").immutable(true).mandatory(true).lazy(true).id(GenericParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>>().name("parameters").immutable(true).many(true).contains(true).id(GenericParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(WILDCARD, WILDCARD_BOUND_TYPE, TYPE, PARAMETERS);
  }

  interface Builder extends IFeaturedObject.Builder<GenericParameter> {
    Builder wildcard(boolean wildcard);
    Builder wildcardBoundType(BoundType wildcardBoundType);
    Builder type(Supplier<Type<?>> type);
    Builder addParameter(Supplier<GenericParameter> parameter);
    Builder addParameters(List<GenericParameter> parameters);
  }
}
