package org.logoce.lmf.model.lang;

import java.lang.String;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.SerializerBuilder;

public interface Serializer extends LMObject {
  static Builder builder() {
    return new SerializerBuilder();
  }

  String toString();
  String fromString();

  interface Features extends LMObject.Features<Features> {
    RawFeature<String, String> toString = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.SERIALIZER.TO_STRING);
    RawFeature<String, String> fromString = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.SERIALIZER.FROM_STRING);
  }

  interface Builder extends IFeaturedObject.Builder<Serializer> {
    Builder toString(String toString);
    Builder fromString(String fromString);
  }
}
