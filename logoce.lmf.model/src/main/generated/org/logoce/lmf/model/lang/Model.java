package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;

public interface Model extends Named {
  String domain();
  List<String> imports();
  List<String> metamodels();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int DOMAIN = -463269570;
    int IMPORTS = -1387965388;
    int METAMODELS = -1744988119;
  }

  interface Features<T extends Features<T>> extends Named.Features<T> {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<String, String> DOMAIN = new AttributeBuilder<String, String>().name("domain").immutable(true).mandatory(true).id(Model.FeatureIDs.DOMAIN).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, List<String>> IMPORTS = new AttributeBuilder<String, List<String>>().name("imports").immutable(true).many(true).id(Model.FeatureIDs.IMPORTS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    Attribute<String, List<String>> METAMODELS = new AttributeBuilder<String, List<String>>().name("metamodels").immutable(true).many(true).id(Model.FeatureIDs.METAMODELS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
    List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, METAMODELS);
  }
}
