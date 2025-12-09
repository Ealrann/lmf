package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.GroupBuilder;

public interface Group<T extends LMObject> extends Type<T>, Concept<T> {
  static <T extends LMObject> Builder<T> builder() {
    return new GroupBuilder<>();
  }

  boolean concrete();
  List<Include<?>> includes();
  List<Feature<?, ?>> features();
  List<Generic<?>> generics();
  List<Operation> operations();
  BuilderSupplier<T> lmBuilder();

  interface Features<T extends Features<T>> extends Type.Features<T>, Concept.Features<T> {
    RawFeature<String, String> name = Named.Features.name;
    RawFeature<Boolean, Boolean> concrete = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.GROUP.CONCRETE);
    RawFeature<Include<?>, List<Include<?>>> includes = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.GROUP.INCLUDES);
    RawFeature<Feature<?, ?>, List<Feature<?, ?>>> features = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.GROUP.FEATURES);
    RawFeature<Generic<?>, List<Generic<?>>> generics = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.GROUP.GENERICS);
    RawFeature<Operation, List<Operation>> operations = new RawFeature<>(true,true,() -> LMCoreModelDefinition.Features.GROUP.OPERATIONS);
    RawFeature<BuilderSupplier<?>, BuilderSupplier<?>> lmBuilder = new RawFeature<>(false,false,() -> LMCoreModelDefinition.Features.GROUP.LM_BUILDER);
  }

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int CONCRETE = -431948311;
    int INCLUDES = 240954415;
    int FEATURES = -139964511;
    int GENERICS = 1961073888;
    int OPERATIONS = -1207043504;
    int LM_BUILDER = 1328379894;
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Group<T>> {
    Builder<T> name(String name);
    Builder<T> concrete(boolean concrete);
    Builder<T> addInclude(Supplier<Include<?>> include);
    Builder<T> addFeature(Supplier<Feature<?, ?>> feature);
    Builder<T> addGeneric(Supplier<Generic<?>> generic);
    Builder<T> addOperation(Supplier<Operation> operation);
    Builder<T> lmBuilder(BuilderSupplier<T> lmBuilder);
    Builder<T> addIncludes(List<Include<?>> includes);
    Builder<T> addFeatures(List<Feature<?, ?>> features);
    Builder<T> addGenerics(List<Generic<?>> generics);
    Builder<T> addOperations(List<Operation> operations);
  }
}
