package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
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

  interface RFeatures<T extends RFeatures<T>> extends Feature.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<Boolean, Boolean> immutable = Feature.RFeatures.immutable;
    RawFeature<Integer, Integer> id = Feature.RFeatures.id;
    RawFeature<Boolean, Boolean> many = Feature.RFeatures.many;
    RawFeature<Boolean, Boolean> mandatory = Feature.RFeatures.mandatory;
    RawFeature<GenericParameter, List<GenericParameter>> parameters = Feature.RFeatures.parameters;
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = Feature.RFeatures.rawFeature;
    RawFeature<Concept<?>, Concept<?>> concept = new RawFeature<>(false,true,() -> Relation.Features.CONCEPT);
    RawFeature<Boolean, Boolean> lazy = new RawFeature<>(false,false,() -> Relation.Features.LAZY);
    RawFeature<Boolean, Boolean> contains = new RawFeature<>(false,false,() -> Relation.Features.CONTAINS);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = Feature.FeatureIDs.IMMUTABLE;
    int ID = Feature.FeatureIDs.ID;
    int MANY = Feature.FeatureIDs.MANY;
    int MANDATORY = Feature.FeatureIDs.MANDATORY;
    int PARAMETERS = Feature.FeatureIDs.PARAMETERS;
    int RAW_FEATURE = Feature.FeatureIDs.RAW_FEATURE;
    int CONCEPT = 1758409075;
    int LAZY = -813813687;
    int CONTAINS = -1308319628;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean> IMMUTABLE = Feature.Features.IMMUTABLE;
    Attribute<Integer, Integer> ID = Feature.Features.ID;
    Attribute<Boolean, Boolean> MANY = Feature.Features.MANY;
    Attribute<Boolean, Boolean> MANDATORY = Feature.Features.MANDATORY;
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = Feature.Features.PARAMETERS;
    Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = Feature.Features.RAW_FEATURE;
    Relation<Concept<?>, Concept<?>> CONCEPT = new RelationBuilder<Concept<?>, Concept<?>>().name("concept").immutable(true).mandatory(true).lazy(true).rawFeature(Relation.RFeatures.concept).id(Relation.FeatureIDs.CONCEPT).concept(() -> LMCoreModelDefinition.Groups.CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.ALL.get(0)).build()).build();
    Attribute<Boolean, Boolean> LAZY = new AttributeBuilder<Boolean, Boolean>().name("lazy").immutable(true).rawFeature(Relation.RFeatures.lazy).id(Relation.FeatureIDs.LAZY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Boolean, Boolean> CONTAINS = new AttributeBuilder<Boolean, Boolean>().name("contains").immutable(true).rawFeature(Relation.RFeatures.contains).id(Relation.FeatureIDs.CONTAINS).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, RAW_FEATURE, CONCEPT, LAZY, CONTAINS);
  }

  interface Builder<UnaryType extends LMObject, EffectiveType> extends IFeaturedObject.Builder<Relation<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);
    Builder<UnaryType, EffectiveType> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType> id(int id);
    Builder<UnaryType, EffectiveType> many(boolean many);
    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
    Builder<UnaryType, EffectiveType> addParameter(Supplier<GenericParameter> parameter);
    Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);
    Builder<UnaryType, EffectiveType> concept(Supplier<Concept<UnaryType>> concept);
    Builder<UnaryType, EffectiveType> lazy(boolean lazy);
    Builder<UnaryType, EffectiveType> contains(boolean contains);
    Builder<UnaryType, EffectiveType> addParameters(List<GenericParameter> parameters);
  }
}
