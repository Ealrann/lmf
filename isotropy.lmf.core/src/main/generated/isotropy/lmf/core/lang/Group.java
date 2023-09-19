package isotropy.lmf.core.lang;

import isotropy.lmf.core.api.feature.RawFeature;
import isotropy.lmf.core.api.model.IFeaturedObject;
import isotropy.lmf.core.lang.builder.GroupBuilder;
import java.lang.Boolean;
import java.lang.String;
import java.util.List;
import java.util.function.Supplier;

public interface Group<T extends LMObject> extends Type<T>, Concept<T> {
  static <T extends LMObject> Builder<T> builder() {
    return new GroupBuilder<>();
  }

  boolean concrete();

  List<Reference<?>> includes();

  List<Feature<?, ?>> features();

  List<Generic<?>> generics();

  interface Features extends Type.Features<Features>, Concept.Features<Features> {
    RawFeature<String, String> name = Named.Features.name;

    RawFeature<Boolean, Boolean> concrete = new RawFeature<>(false,false,() -> LMCoreDefinition.Features.GROUP.CONCRETE);

    RawFeature<Reference<?>, List<Reference<?>>> includes = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.GROUP.INCLUDES);

    RawFeature<Feature<?, ?>, List<Feature<?, ?>>> features = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.GROUP.FEATURES);

    RawFeature<Generic<?>, List<Generic<?>>> generics = new RawFeature<>(true,true,() -> LMCoreDefinition.Features.GROUP.GENERICS);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Group<T>> {
    Builder<T> name(String name);

    Builder<T> concrete(boolean concrete);

    Builder<T> addInclude(Supplier<Reference<?>> include);

    Builder<T> addFeature(Supplier<Feature<?, ?>> feature);

    Builder<T> addGeneric(Supplier<Generic<?>> generic);
  }
}
