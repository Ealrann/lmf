package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.IncludeBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Include<T extends LMObject> extends LMObject {
  static <T extends LMObject> Builder<T> builder() {
    return new IncludeBuilder<>();
  }

  Group<T> group();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int GROUP = 450930500;
    int PARAMETERS = -221199291;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Relation<Group<?>, Group<?>, Listener<Group<?>>, Features<?>> GROUP = new RelationBuilder<Group<?>, Group<?>, Listener<Group<?>>, Features<?>>().name("group").immutable(true).mandatory(true).lazy(true).id(Include.FeatureIDs.GROUP).concept(() -> LMCoreModelDefinition.Groups.GROUP).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.INCLUDE.ALL.get(0)).build()).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>>().name("parameters").immutable(true).many(true).contains(true).id(Include.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(GROUP, PARAMETERS);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Include<T>> {
    Builder<T> group(Supplier<Group<T>> group);
    Builder<T> addParameter(Supplier<GenericParameter> parameter);
    Builder<T> addParameters(List<GenericParameter> parameters);
  }
}
