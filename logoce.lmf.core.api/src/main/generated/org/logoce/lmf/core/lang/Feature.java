package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.BooleanListener;
import org.logoce.lmf.core.api.notification.listener.IntListener;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;

public interface Feature<UnaryType, EffectiveType, ListenerType, ParentGroup> extends Named {
  @Override
  IModelNotifier<? extends Features<?>> notifier();
  boolean immutable();
  int id();
  boolean many();
  boolean mandatory();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = 171805023;
    int ID = -1923216418;
    int MANY = -1374920606;
    int MANDATORY = -1818093130;
    int PARAMETERS = -772256339;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> IMMUTABLE = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("immutable").immutable(true).id(Feature.FeatureIDs.IMMUTABLE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Integer, Integer, IntListener, Features<?>> ID = new AttributeBuilder<Integer, Integer, IntListener, Features<?>>().name("id").immutable(true).id(Feature.FeatureIDs.ID).datatype(() -> LMCoreModelDefinition.Units.INT).build();
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> MANY = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("many").immutable(true).id(Feature.FeatureIDs.MANY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> MANDATORY = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("mandatory").immutable(true).id(Feature.FeatureIDs.MANDATORY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Features<?>>().name("parameters").immutable(true).many(true).contains(true).id(Feature.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS);
  }
}
