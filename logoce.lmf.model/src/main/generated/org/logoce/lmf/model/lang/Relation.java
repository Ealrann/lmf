package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType> {
  static <UnaryType extends LMObject, EffectiveType> Builder<UnaryType, EffectiveType> builder() {
    return new RelationBuilder<>();
  }

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
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean> IMMUTABLE = Feature.Features.IMMUTABLE;
    Attribute<Integer, Integer> ID = Feature.Features.ID;
    Attribute<Boolean, Boolean> MANY = Feature.Features.MANY;
    Attribute<Boolean, Boolean> MANDATORY = Feature.Features.MANDATORY;
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = Feature.Features.PARAMETERS;
    Relation<Concept<?>, Concept<?>> CONCEPT = new RelationBuilder<Concept<?>, Concept<?>>().name("concept").immutable(true).mandatory(true).lazy(true).id(Relation.FeatureIDs.CONCEPT).concept(() -> LMCoreModelDefinition.Groups.CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.ALL.get(0)).build()).build();
    Attribute<Boolean, Boolean> LAZY = new AttributeBuilder<Boolean, Boolean>().name("lazy").immutable(true).id(Relation.FeatureIDs.LAZY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Boolean, Boolean> CONTAINS = new AttributeBuilder<Boolean, Boolean>().name("contains").immutable(true).id(Relation.FeatureIDs.CONTAINS).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, CONCEPT, LAZY, CONTAINS);
  }

  interface Builder<UnaryType extends LMObject, EffectiveType> extends IFeaturedObject.Builder<Relation<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);
    Builder<UnaryType, EffectiveType> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType> id(int id);
    Builder<UnaryType, EffectiveType> many(boolean many);
    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
    Builder<UnaryType, EffectiveType> addParameter(Supplier<GenericParameter> parameter);
    Builder<UnaryType, EffectiveType> concept(Supplier<Concept<UnaryType>> concept);
    Builder<UnaryType, EffectiveType> lazy(boolean lazy);
    Builder<UnaryType, EffectiveType> contains(boolean contains);
    Builder<UnaryType, EffectiveType> addParameters(List<GenericParameter> parameters);
  }
}
