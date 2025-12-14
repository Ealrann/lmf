package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.UnitBuilder;
import org.logoce.lmf.core.notification.listener.Listener;

public interface Unit<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new UnitBuilder<>();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  String matcher();
  String defaultValue();
  Primitive primitive();
  String extractor();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int MATCHER = 1032045637;
    int DEFAULT_VALUE = -221625219;
    int PRIMITIVE = 1504038714;
    int EXTRACTOR = -1208971145;
  }

  interface Features<T extends Features<T>> extends Datatype.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, Features<?>> MATCHER = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("matcher").immutable(true).id(Unit.FeatureIDs.MATCHER).datatype(() -> LMCoreModelDefinition.Units.MATCHER).build();
    Attribute<String, String, Listener<String>, Features<?>> DEFAULT_VALUE = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("defaultValue").immutable(true).id(Unit.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<Primitive, Primitive, Listener<Primitive>, Features<?>> PRIMITIVE = new AttributeBuilder<Primitive, Primitive, Listener<Primitive>, Features<?>>().name("primitive").immutable(true).defaultValue("String").id(Unit.FeatureIDs.PRIMITIVE).datatype(() -> LMCoreModelDefinition.Enums.PRIMITIVE).build();
    Attribute<String, String, Listener<String>, Features<?>> EXTRACTOR = new AttributeBuilder<String, String, Listener<String>, Features<?>>().name("extractor").immutable(true).id(Unit.FeatureIDs.EXTRACTOR).datatype(() -> LMCoreModelDefinition.Units.EXTRACTOR).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, MATCHER, DEFAULT_VALUE, PRIMITIVE, EXTRACTOR);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Unit<T>> {
    Builder<T> name(String name);
    Builder<T> matcher(String matcher);
    Builder<T> defaultValue(String defaultValue);
    Builder<T> primitive(Primitive primitive);
    Builder<T> extractor(String extractor);
  }
}
