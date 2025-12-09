package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.UnitBuilder;

public interface Unit<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new UnitBuilder<>();
  }

  String matcher();
  String defaultValue();
  Primitive primitive();
  String extractor();

  interface RFeatures<T extends RFeatures<T>> extends Datatype.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, String> matcher = new RawFeature<>(false,false,() -> Unit.Features.MATCHER);
    RawFeature<String, String> defaultValue = new RawFeature<>(false,false,() -> Unit.Features.DEFAULT_VALUE);
    RawFeature<Primitive, Primitive> primitive = new RawFeature<>(false,false,() -> Unit.Features.PRIMITIVE);
    RawFeature<String, String> extractor = new RawFeature<>(false,false,() -> Unit.Features.EXTRACTOR);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int MATCHER = 1032045637;
    int DEFAULT_VALUE = -221625219;
    int PRIMITIVE = 1504038714;
    int EXTRACTOR = -1208971145;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> MATCHER = new AttributeBuilder<String, String>().name("matcher").immutable(true).rawFeature(Unit.RFeatures.matcher).id(Unit.FeatureIDs.MATCHER).datatype(() -> LMCoreModelDefinition.Units.MATCHER).build();
    Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).rawFeature(Unit.RFeatures.defaultValue).id(Unit.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<Primitive, Primitive> PRIMITIVE = new AttributeBuilder<Primitive, Primitive>().name("primitive").immutable(true).defaultValue("String").rawFeature(Unit.RFeatures.primitive).id(Unit.FeatureIDs.PRIMITIVE).datatype(() -> LMCoreModelDefinition.Enums.PRIMITIVE).build();
    Attribute<String, String> EXTRACTOR = new AttributeBuilder<String, String>().name("extractor").immutable(true).rawFeature(Unit.RFeatures.extractor).id(Unit.FeatureIDs.EXTRACTOR).datatype(() -> LMCoreModelDefinition.Units.EXTRACTOR).build();
    List<Feature<?, ?>> ALL = List.of(NAME, MATCHER, DEFAULT_VALUE, PRIMITIVE, EXTRACTOR);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Unit<T>> {
    Builder<T> name(String name);
    Builder<T> matcher(String matcher);
    Builder<T> defaultValue(String defaultValue);
    Builder<T> primitive(Primitive primitive);
    Builder<T> extractor(String extractor);
  }
}
