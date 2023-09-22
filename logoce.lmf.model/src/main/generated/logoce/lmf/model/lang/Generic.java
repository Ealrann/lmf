package logoce.lmf.model.lang;

import java.lang.String;
import java.util.function.Supplier;
import logoce.lmf.model.api.feature.RawFeature;
import logoce.lmf.model.api.model.IFeaturedObject;
import logoce.lmf.model.lang.builder.GenericBuilder;

public interface Generic<T extends LMObject> extends Concept<T> {
  static <T extends LMObject> Builder<T> builder() {
    return new GenericBuilder<>();
  }

  Type<T> type();

  BoundType boundType();

  interface Features extends Concept.Features<Features> {
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
