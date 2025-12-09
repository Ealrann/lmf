package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;

public interface Feature<UnaryType, EffectiveType> extends Named {
  boolean immutable();
  int id();
  boolean many();
  boolean mandatory();
  List<GenericParameter> parameters();
  RawFeature<UnaryType, EffectiveType> rawFeature();

  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<Boolean, Boolean> immutable = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.FEATURE.IMMUTABLE);
    RawFeature<Integer, Integer> id = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.FEATURE.ID);
    RawFeature<Boolean, Boolean> many = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.FEATURE.MANY);
    RawFeature<Boolean, Boolean> mandatory = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.FEATURE.MANDATORY);
    RawFeature<GenericParameter, List<GenericParameter>> parameters = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.FEATURE.PARAMETERS);
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.FEATURE.RAW_FEATURE);
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
}
