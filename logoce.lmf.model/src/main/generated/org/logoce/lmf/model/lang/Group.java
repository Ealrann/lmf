package org.logoce.lmf.model.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.GroupBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;

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

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int CONCRETE = -431948311;
    int INCLUDES = 240954415;
    int FEATURES = -139964511;
    int GENERICS = 1961073888;
    int OPERATIONS = -1207043504;
    int LM_BUILDER = 1328379894;
  }

  interface Features<T extends Features<T>> extends Type.Features<T>, Concept.Features<T> {
    Attribute<String, String> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean> CONCRETE = new AttributeBuilder<Boolean, Boolean>().name("concrete").immutable(true).id(Group.FeatureIDs.CONCRETE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Relation<Include<?>, List<Include<?>>> INCLUDES = new RelationBuilder<Include<?>, List<Include<?>>>().name("includes").immutable(true).many(true).contains(true).id(Group.FeatureIDs.INCLUDES).concept(() -> LMCoreModelDefinition.Groups.INCLUDE).build();
    Relation<Feature<?, ?>, List<Feature<?, ?>>> FEATURES = new RelationBuilder<Feature<?, ?>, List<Feature<?, ?>>>().name("features").immutable(true).many(true).contains(true).id(Group.FeatureIDs.FEATURES).concept(() -> LMCoreModelDefinition.Groups.FEATURE).build();
    Relation<Generic<?>, List<Generic<?>>> GENERICS = new RelationBuilder<Generic<?>, List<Generic<?>>>().name("generics").immutable(true).many(true).contains(true).id(Group.FeatureIDs.GENERICS).concept(() -> LMCoreModelDefinition.Groups.GENERIC).build();
    Relation<Operation, List<Operation>> OPERATIONS = new RelationBuilder<Operation, List<Operation>>().name("operations").immutable(true).many(true).contains(true).id(Group.FeatureIDs.OPERATIONS).concept(() -> LMCoreModelDefinition.Groups.OPERATION).build();
    Attribute<BuilderSupplier<?>, BuilderSupplier<?>> LM_BUILDER = new AttributeBuilder<BuilderSupplier<?>, BuilderSupplier<?>>().name("lmBuilder").immutable(true).mandatory(true).id(Group.FeatureIDs.LM_BUILDER).datatype(() -> LMCoreModelDefinition.JavaWrappers.BUILDER_SUPPLIER).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.ALL.get(0)).build()).build();
    List<Feature<?, ?>> ALL = List.of(NAME, CONCRETE, INCLUDES, FEATURES, GENERICS, OPERATIONS, LM_BUILDER);
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
