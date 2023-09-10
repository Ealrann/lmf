package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import java.lang.String;

public interface Concept<T> extends Named {
  interface Features {
    RawFeature<String, String> name = Named.Features.name;
  }
}
