package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.lang.builder.JavaWrapperBuilder;
import java.lang.String;

public interface JavaWrapper<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new JavaWrapperBuilder<>();
  }

  String domain();

  interface Features {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, String> domain = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.JAVA_WRAPPER.DOMAIN);
  }

  interface Builder<T> extends IFeaturedObject.Builder<JavaWrapper<T>> {
    Builder<T> name(String name);

    Builder<T> domain(String domain);
  }
}
