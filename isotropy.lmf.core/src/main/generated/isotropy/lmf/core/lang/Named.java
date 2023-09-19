package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import java.lang.String;

public interface Named extends LMObject {
  String name();

  interface Features<T extends Features<T>> extends LMObject.Features<T> {
    RawFeature<String, String> name = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.NAMED.NAME);
  }
}
