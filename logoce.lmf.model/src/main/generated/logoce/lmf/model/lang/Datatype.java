package logoce.lmf.model.lang;

import java.lang.String;
import logoce.lmf.model.api.feature.RawFeature;

public interface Datatype<T> extends Type<T> {
  interface Features<T extends Features<T>> extends Type.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
  }
}
