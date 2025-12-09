package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;

public interface Model extends Named {
  String domain();
  List<String> imports();
  List<String> metamodels();

  interface RFeatures<T extends RFeatures<T>> extends Named.RFeatures<T> {
    RawFeature<String, String> name = Named.RFeatures.name;
    RawFeature<String, String> domain = new RawFeature<>(false,false,() -> Model.Features.DOMAIN);
    RawFeature<String, List<String>> imports = new RawFeature<>(true,false,() -> Model.Features.IMPORTS);
    RawFeature<String, List<String>> metamodels = new RawFeature<>(true,false,() -> Model.Features.METAMODELS);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int DOMAIN = -463269570;
    int IMPORTS = -1387965388;
    int METAMODELS = -1744988119;
  }

  interface Features {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> DOMAIN = new AttributeBuilder<String, String>().name("domain").immutable(true).mandatory(true).rawFeature(Model.RFeatures.domain).id(Model.FeatureIDs.DOMAIN).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, List<String>> IMPORTS = new AttributeBuilder<String, List<String>>().name("imports").immutable(true).many(true).rawFeature(Model.RFeatures.imports).id(Model.FeatureIDs.IMPORTS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, List<String>> METAMODELS = new AttributeBuilder<String, List<String>>().name("metamodels").immutable(true).many(true).rawFeature(Model.RFeatures.metamodels).id(Model.FeatureIDs.METAMODELS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, METAMODELS);
  }
}
