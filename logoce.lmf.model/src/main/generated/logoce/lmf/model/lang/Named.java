package logoce.lmf.model.lang;

import java.lang.String;
import logoce.lmf.model.api.feature.RawFeature;

public interface Named extends LMObject {
  String name();

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    RawFeature<String, String> name = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.NAMED.NAME);
  }
}
