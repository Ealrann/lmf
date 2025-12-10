package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.BooleanListener;
import org.logoce.lmf.model.notification.listener.IntListener;
import org.logoce.lmf.model.notification.listener.Listener;

public interface Attribute<UnaryType, EffectiveType, ListenerType, ParentGroup extends LMObject> extends Feature<UnaryType, EffectiveType, ListenerType, ParentGroup> {
  static <UnaryType, EffectiveType, ListenerType, ParentGroup extends LMObject> Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> builder(
      ) {
    return new AttributeBuilder<>();
  }

  Datatype<UnaryType> datatype();
  String defaultValue();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = Feature.FeatureIDs.IMMUTABLE;
    int ID = Feature.FeatureIDs.ID;
    int MANY = Feature.FeatureIDs.MANY;
    int MANDATORY = Feature.FeatureIDs.MANDATORY;
    int PARAMETERS = Feature.FeatureIDs.PARAMETERS;
    int DATATYPE = -748350133;
    int DEFAULT_VALUE = 2061499287;
  }

  interface Features<T extends Features<T>> extends Feature.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>> IMMUTABLE = Feature.Features.IMMUTABLE;
    Attribute<Integer, Integer, IntListener, Feature<?, ?, ?, ?>> ID = Feature.Features.ID;
    Attribute<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>> MANY = Feature.Features.MANY;
    Attribute<Boolean, Boolean, BooleanListener, Feature<?, ?, ?, ?>> MANDATORY = Feature.Features.MANDATORY;
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Feature<?, ?, ?, ?>> PARAMETERS = Feature.Features.PARAMETERS;
    Relation<Datatype<?>, Datatype<?>, Listener<Datatype<?>>, Attribute<?, ?, ?, ?>> DATATYPE = new RelationBuilder<Datatype<?>, Datatype<?>, Listener<Datatype<?>>, Attribute<?, ?, ?, ?>>().name("datatype").immutable(true).mandatory(true).lazy(true).id(Attribute.FeatureIDs.DATATYPE).concept(() -> LMCoreModelDefinition.Groups.DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.ALL.get(0)).build()).build();
    Attribute<String, String, Listener<String>, Attribute<?, ?, ?, ?>> DEFAULT_VALUE = new AttributeBuilder<String, String, Listener<String>, Attribute<?, ?, ?, ?>>().name("defaultValue").immutable(true).id(Attribute.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, DATATYPE, DEFAULT_VALUE);
  }

  interface Builder<UnaryType, EffectiveType, ListenerType, ParentGroup extends LMObject> extends IFeaturedObject.Builder<Attribute<UnaryType, EffectiveType, ListenerType, ParentGroup>> {
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> name(String name);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> id(int id);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> many(boolean many);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> mandatory(boolean mandatory);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameter(
        Supplier<GenericParameter> parameter);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> datatype(
        Supplier<Datatype<UnaryType>> datatype);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> defaultValue(String defaultValue);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameters(
        List<GenericParameter> parameters);
  }
}
