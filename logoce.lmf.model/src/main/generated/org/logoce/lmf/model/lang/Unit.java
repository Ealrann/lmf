package org.logoce.lmf.model.lang;

import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.UnitBuilder;

public interface Unit<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new UnitBuilder<>();
  }

  String matcher();
  String defaultValue();
  Primitive primitive();
  String extractor();

  interface Features<T extends Features<T>> extends Datatype.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> matcher = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.UNIT.MATCHER);
    RawFeature<String, String> defaultValue = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.UNIT.DEFAULT_VALUE);
    RawFeature<Primitive, Primitive> primitive = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.UNIT.PRIMITIVE);
    RawFeature<String, String> extractor = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.UNIT.EXTRACTOR);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int MATCHER = 1032045637;
    int DEFAULT_VALUE = -221625219;
    int PRIMITIVE = 1504038714;
    int EXTRACTOR = -1208971145;
  }

  interface Builder<T> extends IFeaturedObject.Builder<Unit<T>> {
    Builder<T> name(String name);
    Builder<T> matcher(String matcher);
    Builder<T> defaultValue(String defaultValue);
    Builder<T> primitive(Primitive primitive);
    Builder<T> extractor(String extractor);
  }
}
