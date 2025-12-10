package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.BooleanListener;
import org.logoce.lmf.model.notification.listener.IntListener;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Feature<UnaryType, EffectiveType, ListenerType, ParentGroup extends LMObject> extends Named {
  boolean immutable();
  int id();
  boolean many();
  boolean mandatory();
  List<GenericParameter> parameters();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = 2122316949;
    int ID = 1492330856;
    int MANY = -389004436;
    int MANDATORY = 132418796;
    int PARAMETERS = -435928777;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>> IMMUTABLE = new AttributeBuilder<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>>().name("immutable").immutable(true).id(Feature.FeatureIDs.IMMUTABLE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Integer, Integer, IntListener, Feature<?, ?, ?, ?>> ID = new AttributeBuilder<Integer, Integer, IntListener, Feature<?, ?, ?, ?>>().name("id").immutable(true).id(Feature.FeatureIDs.ID).datatype(() -> LMCoreModelDefinition.Units.INT).build();
    Attribute<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>> MANY = new AttributeBuilder<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>>().name("many").immutable(true).id(Feature.FeatureIDs.MANY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>> MANDATORY = new AttributeBuilder<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>>().name("mandatory").immutable(true).id(Feature.FeatureIDs.MANDATORY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Feature<?, ?, ?, ?>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Feature<?, ?, ?, ?>>().name("parameters").immutable(true).many(true).contains(true).id(Feature.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS);
  }
}
