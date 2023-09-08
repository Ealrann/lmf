package isotropy.lmf.core.lang;

import isotropy.lmf.core.model.RawFeature;
import java.lang.String;

public interface Datatype<T> extends Type<T> {
  interface Features {
    RawFeature<String, String> name = Named.Features.name;
  }
}
