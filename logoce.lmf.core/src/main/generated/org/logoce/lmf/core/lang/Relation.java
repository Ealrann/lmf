package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;
import org.logoce.lmf.core.notification.listener.BooleanListener;
import org.logoce.lmf.core.notification.listener.IntListener;
import org.logoce.lmf.core.notification.listener.Listener;

public interface Relation<UnaryType extends LMObject, EffectiveType, ListenerType, ParentGroup> extends Feature<UnaryType, EffectiveType, ListenerType, ParentGroup> {
  static <UnaryType extends LMObject, EffectiveType, ListenerType, ParentGroup> Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> builder(
      ) {
    return new RelationBuilder<>();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  Concept<UnaryType> concept();
  boolean lazy();
  boolean contains();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = Feature.FeatureIDs.IMMUTABLE;
    int ID = Feature.FeatureIDs.ID;
    int MANY = Feature.FeatureIDs.MANY;
    int MANDATORY = Feature.FeatureIDs.MANDATORY;
    int PARAMETERS = Feature.FeatureIDs.PARAMETERS;
    int CONCEPT = 1758409075;
    int LAZY = -813813687;
    int CONTAINS = -1308319628;
  }

  interface Features<T extends Features<T>> extends Feature.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean, BooleanListener, Feature.Features<?>> IMMUTABLE = Feature.Features.IMMUTABLE;
    Attribute<Integer, Integer, IntListener, Feature.Features<?>> ID = Feature.Features.ID;
    Attribute<Boolean, Boolean, BooleanListener, Feature.Features<?>> MANY = Feature.Features.MANY;
    Attribute<Boolean, Boolean, BooleanListener, Feature.Features<?>> MANDATORY = Feature.Features.MANDATORY;
    Relation<GenericParameter, List<GenericParameter>, Listener<List<GenericParameter>>, Feature.Features<?>> PARAMETERS = Feature.Features.PARAMETERS;
    Relation<Concept<?>, Concept<?>, Listener<Concept<?>>, Features<?>> CONCEPT = new RelationBuilder<Concept<?>, Concept<?>, Listener<Concept<?>>, Features<?>>().name("concept").immutable(true).mandatory(true).lazy(true).id(Relation.FeatureIDs.CONCEPT).concept(() -> LMCoreModelDefinition.Groups.CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.ALL.get(0)).build()).build();
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> LAZY = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("lazy").immutable(true).id(Relation.FeatureIDs.LAZY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> CONTAINS = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("contains").immutable(true).id(Relation.FeatureIDs.CONTAINS).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, CONCEPT, LAZY, CONTAINS);
  }

  interface Builder<UnaryType extends LMObject, EffectiveType, ListenerType, ParentGroup> extends IFeaturedObject.Builder<Relation<UnaryType, EffectiveType, ListenerType, ParentGroup>> {
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> name(String name);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> id(int id);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> many(boolean many);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> mandatory(boolean mandatory);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameter(
        Supplier<GenericParameter> parameter);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> concept(
        Supplier<Concept<UnaryType>> concept);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> lazy(boolean lazy);
    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> contains(boolean contains);

    Builder<UnaryType, EffectiveType, ListenerType, ParentGroup> addParameters(
        List<GenericParameter> parameters);
  }
}
