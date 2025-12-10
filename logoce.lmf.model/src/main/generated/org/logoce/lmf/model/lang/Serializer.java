package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.SerializerBuilder;

public interface Serializer extends LMObject {
  static Builder builder() {
    return new SerializerBuilder();
  }

  String defaultValue();
  String create();
  String convert();

  interface FeatureIDs {
    int DEFAULT_VALUE = -947412497;
    int CREATE = 2102923899;
    int CONVERT = 689167380;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).id(Serializer.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, String> CREATE = new AttributeBuilder<String, String>().name("create").immutable(true).mandatory(true).id(Serializer.FeatureIDs.CREATE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, String> CONVERT = new AttributeBuilder<String, String>().name("convert").immutable(true).mandatory(true).id(Serializer.FeatureIDs.CONVERT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(DEFAULT_VALUE, CREATE, CONVERT);
  }

  interface Builder extends IFeaturedObject.Builder<Serializer> {
    Builder defaultValue(String defaultValue);
    Builder create(String create);
    Builder convert(String convert);
  }
}
