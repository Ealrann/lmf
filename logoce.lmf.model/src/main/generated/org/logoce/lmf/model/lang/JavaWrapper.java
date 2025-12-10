package org.logoce.lmf.model.lang;

import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.JavaWrapperBuilder;

public interface JavaWrapper<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new JavaWrapperBuilder<>();
  }

  String qualifiedClassName();
  Serializer serializer();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Datatype.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int QUALIFIED_CLASS_NAME = 1292771257;
    int SERIALIZER = -1882174364;
  }

  interface Builder<T> extends IFeaturedObject.Builder<JavaWrapper<T>> {
    Builder<T> name(String name);
    Builder<T> qualifiedClassName(String qualifiedClassName);
    Builder<T> serializer(Supplier<Serializer> serializer);
  }
}
