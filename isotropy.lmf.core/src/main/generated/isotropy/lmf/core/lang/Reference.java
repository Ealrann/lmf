package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.lang.builder.ReferenceBuilder;
import java.util.List;
import java.util.function.Supplier;

public interface Reference<T extends LMObject> extends LMObject {
  static <T extends LMObject> Builder<T> builder() {
    return new ReferenceBuilder<>();
  }

  Concept<T> group();

  List<Concept<?>> parameters();

  interface Features extends LMObject.Features<Features> {
    RawFeature<Concept<?>, Concept<?>> group = new RawFeature<>(false,true,() -> LMCoreDefinition.Features.REFERENCE.GROUP);

    RawFeature<Concept<?>, List<Concept<?>>> parameters = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.REFERENCE.PARAMETERS);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Reference<T>> {
    Builder<T> group(Supplier<Concept<T>> group);

    Builder<T> addParameter(Supplier<Concept<?>> parameter);
  }
}
