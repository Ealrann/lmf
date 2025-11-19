package org.logoce.lmf.model.lang;

import java.lang.Boolean;
import java.lang.String;
import org.logoce.lmf.model.api.feature.RawFeature;

public interface Feature<UnaryType, EffectiveType> extends Named {
  boolean immutable();
  boolean many();
  boolean mandatory();
  RawFeature<UnaryType, EffectiveType> rawFeature();

  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<Boolean, Boolean> immutable = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.FEATURE.IMMUTABLE);
    RawFeature<Boolean, Boolean> many = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.FEATURE.MANY);
    RawFeature<Boolean, Boolean> mandatory = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.FEATURE.MANDATORY);
    RawFeature<RawFeature<?, ?>, RawFeature<?, ?>> rawFeature = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.FEATURE.RAW_FEATURE);
  }
}
