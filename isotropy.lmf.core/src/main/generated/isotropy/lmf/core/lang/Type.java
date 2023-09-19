package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import java.lang.String;

public interface Type<T> extends Named {
  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
  }
}
