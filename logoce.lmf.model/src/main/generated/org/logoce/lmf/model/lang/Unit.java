package org.logoce.lmf.model.lang;

import java.lang.String;
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

  interface Features extends Datatype.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, String> matcher = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.UNIT.MATCHER);

    RawFeature<String, String> defaultValue = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.UNIT.DEFAULT_VALUE);

    RawFeature<Primitive, Primitive> primitive = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.UNIT.PRIMITIVE);

    RawFeature<String, String> extractor = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.UNIT.EXTRACTOR);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Unit<T>> {
    Builder<T> name(String name);

    Builder<T> matcher(String matcher);

    Builder<T> defaultValue(String defaultValue);

    Builder<T> primitive(Primitive primitive);

    Builder<T> extractor(String extractor);
  }
}
