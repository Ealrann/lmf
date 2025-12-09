package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface Attribute<UnaryType, EffectiveType> extends Feature<UnaryType, EffectiveType> {
  static <UnaryType, EffectiveType> Builder<UnaryType, EffectiveType> builder() {
    return new AttributeBuilder<>();
  }

  Datatype<UnaryType> datatype();
  String defaultValue();

  interface RFeatures<T extends RFeatures<T>> extends Feature.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<Boolean, Boolean> immutable = Feature.RFeatures.immutable;
    RawFeature<Integer, Integer> id = Feature.RFeatures.id;
    RawFeature<Boolean, Boolean> many = Feature.RFeatures.many;
    RawFeature<Boolean, Boolean> mandatory = Feature.RFeatures.mandatory;
    RawFeature<GenericParameter, List<GenericParameter>> parameters = Feature.RFeatures.parameters;
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = Feature.RFeatures.rawFeature;
    RawFeature<Datatype<?>, Datatype<?>> datatype = new RawFeature<>(false,true,() -> Attribute.Features.DATATYPE);
    RawFeature<String, String> defaultValue = new RawFeature<>(false,false,() -> Attribute.Features.DEFAULT_VALUE);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int IMMUTABLE = Feature.FeatureIDs.IMMUTABLE;
    int ID = Feature.FeatureIDs.ID;
    int MANY = Feature.FeatureIDs.MANY;
    int MANDATORY = Feature.FeatureIDs.MANDATORY;
    int PARAMETERS = Feature.FeatureIDs.PARAMETERS;
    int RAW_FEATURE = Feature.FeatureIDs.RAW_FEATURE;
    int DATATYPE = -748350133;
    int DEFAULT_VALUE = 2061499287;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean> IMMUTABLE = Feature.Features.IMMUTABLE;
    Attribute<Integer, Integer> ID = Feature.Features.ID;
    Attribute<Boolean, Boolean> MANY = Feature.Features.MANY;
    Attribute<Boolean, Boolean> MANDATORY = Feature.Features.MANDATORY;
    Relation<GenericParameter, List<GenericParameter>> PARAMETERS = Feature.Features.PARAMETERS;
    Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = Feature.Features.RAW_FEATURE;
    Relation<Datatype<?>, Datatype<?>> DATATYPE = new RelationBuilder<Datatype<?>, Datatype<?>>().name("datatype").immutable(true).mandatory(true).rawFeature(Attribute.RFeatures.datatype).id(Attribute.FeatureIDs.DATATYPE).concept(() -> LMCoreModelDefinition.Groups.DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.ALL.get(0)).build()).build();
    Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).rawFeature(Attribute.RFeatures.defaultValue).id(Attribute.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, RAW_FEATURE, DATATYPE, DEFAULT_VALUE);
  }

  interface Builder<UnaryType, EffectiveType> extends IFeaturedObject.Builder<Attribute<UnaryType, EffectiveType>> {
    Builder<UnaryType, EffectiveType> name(String name);
    Builder<UnaryType, EffectiveType> immutable(boolean immutable);
    Builder<UnaryType, EffectiveType> id(int id);
    Builder<UnaryType, EffectiveType> many(boolean many);
    Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
    Builder<UnaryType, EffectiveType> addParameter(Supplier<GenericParameter> parameter);
    Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);
    Builder<UnaryType, EffectiveType> datatype(Supplier<Datatype<UnaryType>> datatype);
    Builder<UnaryType, EffectiveType> defaultValue(String defaultValue);
    Builder<UnaryType, EffectiveType> addParameters(List<GenericParameter> parameters);
  }
}
