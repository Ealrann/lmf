package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.lang.builder.UnitBuilder;
import java.lang.String;

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
