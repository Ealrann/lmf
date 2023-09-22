package logoce.lmf.model.lang;

import java.lang.String;
import logoce.lmf.model.api.feature.RawFeature;

public interface Concept<T> extends Named {
  interface Features<T extends Features<T>> extends Named.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
  }
}
