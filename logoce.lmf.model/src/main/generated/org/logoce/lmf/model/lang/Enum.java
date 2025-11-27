package org.logoce.lmf.model.lang;

import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.EnumBuilder;

public interface Enum<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new EnumBuilder<>();
  }

  List<String> literals();

  interface Features<T extends Features<T>> extends Datatype.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<String, List<String>> literals = new RawFeature<>(true,false,() -> LMCoreDefinition.Features.ENUM.LITERALS);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Enum<T>> {
    Builder<T> name(String name);
    Builder<T> addLiteral(String literal);
    Builder<T> addLiterals(List<String> literals);
  }
}
