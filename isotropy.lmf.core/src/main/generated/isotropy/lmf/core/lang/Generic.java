package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.lang.builder.GenericBuilder;
import java.lang.String;
import java.util.function.Supplier;

public interface Generic<T extends LMObject> extends Concept<T> {
  static <T extends LMObject> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  Type<T> type();

  BoundType boundType();

  interface Features {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<Type<?>, Type<?>> type = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.GENERIC.TYPE);

    RawFeature<BoundType, BoundType> boundType = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GENERIC.BOUND_TYPE);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Generic<T>> {
    Builder<T> name(String name);

    Builder<T> type(Supplier<Type<T>> type);

    Builder<T> boundType(BoundType boundType);
  }
}
