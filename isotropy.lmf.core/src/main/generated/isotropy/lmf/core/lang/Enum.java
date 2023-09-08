package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.EnumBuilder;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.RawFeature;
import java.lang.String;
import java.util.List;

public interface Enum<T> extends Datatype<T> {
  static <T> Builder<T> builder() {
    return new EnumBuilder<>();
  }

  List<String> literals();

  interface Features {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<String, List<String>> literals = new RawFeature<>(true,false,() -> LMCoreDefinition.Features.ENUM.LITERALS);
  }

  interface Builder<T> extends IFeaturedObject.Builder<Enum<T>> {
    Builder<T> name(String name);

    Builder<T> addLiteral(String literal);
  }
}
