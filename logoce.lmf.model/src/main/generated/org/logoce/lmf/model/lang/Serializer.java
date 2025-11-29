package org.logoce.lmf.model.lang;

import java.lang.String;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.SerializerBuilder;

public interface Serializer extends LMObject {
  static Builder builder() {
    return new SerializerBuilder();
  }

  String defaultValue();
  String create();
  String convert();

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    RawFeature<String, String> defaultValue = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.SERIALIZER.DEFAULT_VALUE);
    RawFeature<String, String> create = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.SERIALIZER.CREATE);
    RawFeature<String, String> convert = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.SERIALIZER.CONVERT);
  }

  interface Builder extends IFeaturedObject.Builder<Serializer> {
    Builder defaultValue(String defaultValue);
    Builder create(String create);
    Builder convert(String convert);
  }
}
