package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface Feature<UnaryType, EffectiveType> extends Named {
  boolean immutable();
  int id();
  boolean many();
  boolean mandatory();
  List<GenericParameter> parameters();
  RawFeature<UnaryType, EffectiveType> rawFeature();

  interface RFeatures<T extends RFeatures<T>> extends Named.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<Boolean, Boolean> immutable = new RawFeature<>(false,false,() -> Feature.Features.IMMUTABLE);
    RawFeature<Integer, Integer> id = new RawFeature<>(false,false,() -> Feature.Features.ID);
    RawFeature<Boolean, Boolean> many = new RawFeature<>(false,false,() -> Feature.Features.MANY);
    RawFeature<Boolean, Boolean> mandatory = new RawFeature<>(false,false,() -> Feature.Features.MANDATORY);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> Feature.Features.PARAMETERS);
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = new RawFeature<>(false,false,() -> Feature.Features.RAW_FEATURE);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = 2122316949;
    int ID = 1492330856;
    int MANY = -389004436;
    int MANDATORY = 132418796;
    int PARAMETERS = -435928777;
    int RAW_FEATURE = 869653755;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean> IMMUTABLE = new AttributeBuilder<Boolean, Boolean>().name("immutable").immutable(true).rawFeature(Feature.RFeatures.immutable).id(Feature.FeatureIDs.IMMUTABLE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Integer, Integer> ID = new AttributeBuilder<Integer, Integer>().name("id").immutable(true).rawFeature(Feature.RFeatures.id).id(Feature.FeatureIDs.ID).datatype(() -> LMCoreModelDefinition.Units.INT).build();
    Attribute<Boolean, Boolean> MANY = new AttributeBuilder<Boolean, Boolean>().name("many").immutable(true).rawFeature(Feature.RFeatures.many).id(Feature.FeatureIDs.MANY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Attribute<Boolean, Boolean> MANDATORY = new AttributeBuilder<Boolean, Boolean>().name("mandatory").immutable(true).rawFeature(Feature.RFeatures.mandatory).id(Feature.FeatureIDs.MANDATORY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).rawFeature(Feature.RFeatures.parameters).id(Feature.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
    Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = new AttributeBuilder<RawFeature<?, ?>, RawFeature<?, ?>>().name("rawFeature").immutable(true).rawFeature(Feature.RFeatures.rawFeature).id(Feature.FeatureIDs.RAW_FEATURE).datatype(() -> LMCoreModelDefinition.JavaWrappers.RAW_FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.FEATURE.ALL.get(0)).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.FEATURE.ALL.get(1)).build()).build();
    List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, RAW_FEATURE);
  }
}
