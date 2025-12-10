package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.EnumBuilder;

public interface Enum<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new EnumBuilder<>();
  }

  List<String> literals();

  interface FeatureIDs<T extends FeatureIDs<T>> extends Datatype.FeatureIDs<T> {
    int NAME = Named.FeatureIDs.NAME;
    int LITERALS = 384564756;
  }

  interface Builder<T> extends IFeaturedObject.Builder<Enum<T>> {
    Builder<T> name(String name);
    Builder<T> addLiteral(String literal);
    Builder<T> addLiterals(List<String> literals);
  }
}
