package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.SerializerBuilder;

public interface Serializer extends LMObject {
  static Builder builder() {
    return new SerializerBuilder();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  String defaultValue();
  String create();
  String convert();

  interface FeatureIDs {
    int DEFAULT_VALUE = 2006380345;
    int CREATE = 152411973;
    int CONVERT = 352839818;
  }

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    Attribute<String, String, Listener<String>, Features<?>> DEFAULT_VALUE = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("defaultValue").immutable(true).id(Serializer.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, String, Listener<String>, Features<?>> CREATE = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("create").immutable(true).mandatory(true).id(Serializer.FeatureIDs.CREATE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, String, Listener<String>, Features<?>> CONVERT = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("convert").immutable(true).mandatory(true).id(Serializer.FeatureIDs.CONVERT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(DEFAULT_VALUE, CREATE, CONVERT);
  }

  interface Builder extends IFeaturedObject.Builder<Serializer> {
    Builder defaultValue(String defaultValue);
    Builder create(String create);
    Builder convert(String convert);
  }
}
