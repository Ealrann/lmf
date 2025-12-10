package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.BooleanListener;
import org.logoce.lmf.model.notification.listener.Listener;

public interface GenericParameter extends LMObject {
  static Builder builder() {
    return new GenericParameterBuilder();
  }

  boolean wildcard();
  BoundType wildcardBoundType();
  Type<?> type();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int WILDCARD = 520310873;
    int WILDCARD_BOUND_TYPE = 673018239;
    int TYPE = -1625571015;
    int PARAMETERS = -47630167;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Attribute<Boolean, Boolean, BooleanListener, GenericParameter> WILDCARD = new AttributeBuilder<Boolean, Boolean, BooleanListener, GenericParameter>().name("wildcard").immutable(true).id(GenericParameter.FeatureIDs.WILDCARD).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<BoundType, BoundType, Listener<BoundType>, GenericParameter> WILDCARD_BOUND_TYPE = new AttributeBuilder<BoundType, BoundType, Listener<BoundType>, GenericParameter>().name("wildcardBoundType").immutable(true).id(GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
    Relation<Type<?>, Type<?>, Listener<Type<?>>, GenericParameter> TYPE = new RelationBuilder<Type<?>, Type<?>, Listener<Type<?>>, GenericParameter>().name("type").immutable(true).mandatory(true).lazy(true).id(GenericParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, GenericParameter> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, GenericParameter>().name("parameters").immutable(true).many(true).contains(true).id(GenericParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
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
