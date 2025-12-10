package org.logoce.lmf.model.lang;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.SerializerBuilder;

public interface Serializer extends LMObject {
  static Builder builder() {
    return new SerializerBuilder();
  }

  String defaultValue();
  String create();
  String convert();

  interface FeatureIDs<T extends FeatureIDs<T>> extends LMObject.FeatureIDs<T> {
    int DEFAULT_VALUE = -947412497;
    int CREATE = 2102923899;
    int CONVERT = 689167380;
  }

  interface Builder extends IFeaturedObject.Builder<Serializer> {
    Builder defaultValue(String defaultValue);
    Builder create(String create);
    Builder convert(String convert);
  }
}
