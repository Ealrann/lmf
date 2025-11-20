package org.logoce.lmf.model.lang;

import java.lang.String;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.JavaWrapperBuilder;

public interface JavaWrapper<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new JavaWrapperBuilder<>();
  }

  String qualifiedClassName();

  interface Features extends Datatype.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, String> qualifiedClassName = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.JAVA_WRAPPER.QUALIFIED_CLASS_NAME);
  }

  interface Builder<T> extends IFeaturedObject.Builder<JavaWrapper<T>> {
    Builder<T> name(String name);
    Builder<T> qualifiedClassName(String qualifiedClassName);
  }
}
