package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.JavaWrapperBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

public interface JavaWrapper<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new JavaWrapperBuilder<>();
  }

  String qualifiedClassName();
  Serializer serializer();

  interface RFeatures<T extends RFeatures<T>> extends Datatype.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, String> qualifiedClassName = new RawFeature<>(false,false,() -> JavaWrapper.Features.QUALIFIED_CLASS_NAME);
    RawFeature<Serializer, Serializer> serializer = new RawFeature<>(false,true,() -> JavaWrapper.Features.SERIALIZER);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int QUALIFIED_CLASS_NAME = 1292771257;
    int SERIALIZER = -1882174364;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> QUALIFIED_CLASS_NAME = new AttributeBuilder<String, String>().name("qualifiedClassName").immutable(true).mandatory(true).rawFeature(JavaWrapper.RFeatures.qualifiedClassName).id(JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Relation<Serializer, Serializer> SERIALIZER = new RelationBuilder<Serializer, Serializer>().name("serializer").immutable(true).contains(true).rawFeature(JavaWrapper.RFeatures.serializer).id(JavaWrapper.FeatureIDs.SERIALIZER).concept(() -> LMCoreModelDefinition.Groups.SERIALIZER).build();
    List<Feature<?, ?>> ALL = List.of(NAME, QUALIFIED_CLASS_NAME, SERIALIZER);
  }

  interface Builder<T> extends IFeaturedObject.Builder<JavaWrapper<T>> {
    Builder<T> name(String name);
    Builder<T> qualifiedClassName(String qualifiedClassName);
    Builder<T> serializer(Supplier<Serializer> serializer);
  }
}
