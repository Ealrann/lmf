package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.JavaWrapperBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.notification.listener.Listener;

public interface JavaWrapper<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new JavaWrapperBuilder<>();
  }

  String qualifiedClassName();
  Serializer serializer();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int QUALIFIED_CLASS_NAME = 1292771257;
    int SERIALIZER = -1882174364;
  }

  interface Features<T extends Features<T>> extends Datatype.Features<T> {
    Attribute<String, String, Listener<String>, Named> NAME = Named.Features.NAME;
    Attribute<String, String, Listener<String>, JavaWrapper<?>> QUALIFIED_CLASS_NAME = new AttributeBuilder<String, String, Listener<String>, JavaWrapper<?>>().name("qualifiedClassName").immutable(true).mandatory(true).id(JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Relation<Serializer, Serializer, Listener<Serializer>, JavaWrapper<?>> SERIALIZER = new RelationBuilder<Serializer, Serializer, Listener<Serializer>, JavaWrapper<?>>().name("serializer").immutable(true).contains(true).id(JavaWrapper.FeatureIDs.SERIALIZER).concept(() -> LMCoreModelDefinition.Groups.SERIALIZER).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, QUALIFIED_CLASS_NAME, SERIALIZER);
  }

  interface Builder<T> extends IFeaturedObject.Builder<JavaWrapper<T>> {
    Builder<T> name(String name);
    Builder<T> qualifiedClassName(String qualifiedClassName);
    Builder<T> serializer(Supplier<Serializer> serializer);
  }
}
