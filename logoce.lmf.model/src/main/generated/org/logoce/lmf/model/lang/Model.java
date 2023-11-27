package org.logoce.lmf.model.lang;

import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;

public interface Model extends Named {
  String domain();

  List<String> imports();

  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, String> domain = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.MODEL.DOMAIN);

    RawFeature<String, List<String>> imports = new RawFeature<>(true,false,() -> LMCoreDefinition.Features.MODEL.IMPORTS);
  }
}
