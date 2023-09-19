package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import java.lang.String;

public interface Datatype<T> extends Type<T> {
  interface Features<T extends Features<T>> extends Type.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
  }
}
