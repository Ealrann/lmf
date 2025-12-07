package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;

public interface Model extends Named {
  String domain();
  List<String> imports();
  List<String> metamodels();

  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> domain = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.MODEL.DOMAIN);
    RawFeature<String, List<String>> imports = new RawFeature<>(true,false,() -> LMCoreModelDefinition.Features.MODEL.IMPORTS);
    RawFeature<String, List<String>> metamodels = new RawFeature<>(true,false,() -> LMCoreModelDefinition.Features.MODEL.METAMODELS);
  }
}
