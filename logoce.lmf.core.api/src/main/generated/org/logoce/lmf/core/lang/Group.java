package org.logoce.lmf.core.lang;

import java.util.List;
import java.util.function.Supplier;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.notification.listener.BooleanListener;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.core.lang.builder.GroupBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;

public interface Group<T extends LMObject> extends Type<T>, Concept<T> {
  static <T extends LMObject> Builder<T> builder() {
    return new GroupBuilder<>();
  }

  @Override
  IModelNotifier<? extends Features<?>> notifier();
  boolean concrete();
  List<Include<?>> includes();
  List<Feature<?, ?, ?, ?>> features();
  List<Generic<?>> generics();
  List<Operation> operations();
  BuilderSupplier<T> lmBuilder();

  interface FeatureIDs {
    int NAME = Named.FeatureIDs.NAME;
    int CONCRETE = 1290384735;
    int INCLUDES = 1963287461;
    int FEATURES = 1582368535;
    int GENERICS = -611560362;
    int OPERATIONS = 392604742;
    int LM_BUILDER = -1113870528;
  }

  interface Features<T extends Features<T>> extends Type.Features<T>, Concept.Features<T> {
    Attribute<String, String, Listener<String>, Named.Features<?>> NAME = Named.Features.NAME;
    Attribute<Boolean, Boolean, BooleanListener, Features<?>> CONCRETE = new AttributeBuilder<Boolean, Boolean, BooleanListener, Features<?>>().name("concrete").immutable(true).id(Group.FeatureIDs.CONCRETE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
    Relation<Include<?>, List<Include<?>>, Listener<List<Include<?>>>, Features<?>> INCLUDES = new RelationBuilder<Include<?>, List<Include<?>>, Listener<List<Include<?>>>, Features<?>>().name("includes").immutable(true).many(true).contains(true).id(Group.FeatureIDs.INCLUDES).concept(() -> LMCoreModelDefinition.Groups.INCLUDE).build();
    Relation<Feature<?, ?, ?, ?>, List<Feature<?, ?, ?, ?>>, Listener<List<Feature<?, ?, ?, ?>>>, Features<?>> FEATURES = new RelationBuilder<Feature<?, ?, ?, ?>, List<Feature<?, ?, ?, ?>>, Listener<List<Feature<?, ?, ?, ?>>>, Features<?>>().name("features").immutable(true).many(true).contains(true).id(Group.FeatureIDs.FEATURES).concept(() -> LMCoreModelDefinition.Groups.FEATURE).build();
    Relation<Generic<?>, List<Generic<?>>, Listener<List<Generic<?>>>, Features<?>> GENERICS = new RelationBuilder<Generic<?>, List<Generic<?>>, Listener<List<Generic<?>>>, Features<?>>().name("generics").immutable(true).many(true).contains(true).id(Group.FeatureIDs.GENERICS).concept(() -> LMCoreModelDefinition.Groups.GENERIC).build();
    Relation<Operation, List<Operation>, Listener<List<Operation>>, Features<?>> OPERATIONS = new RelationBuilder<Operation, List<Operation>, Listener<List<Operation>>, Features<?>>().name("operations").immutable(true).many(true).contains(true).id(Group.FeatureIDs.OPERATIONS).concept(() -> LMCoreModelDefinition.Groups.OPERATION).build();
    Attribute<BuilderSupplier<?>, BuilderSupplier<?>, Listener<BuilderSupplier<?>>, Features<?>> LM_BUILDER = new AttributeBuilder<BuilderSupplier<?>, BuilderSupplier<?>, Listener<BuilderSupplier<?>>, Features<?>>().name("lmBuilder").immutable(true).mandatory(true).id(Group.FeatureIDs.LM_BUILDER).datatype(() -> (Datatype<BuilderSupplier<?>>) (Datatype) LMCoreModelDefinition.JavaWrappers.BUILDER_SUPPLIER).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.T).build()).build();
    List<Feature<?, ?, ?, ?>> ALL = List.of(NAME, CONCRETE, INCLUDES, FEATURES, GENERICS, OPERATIONS, LM_BUILDER);
  }

  interface Builder<T extends LMObject> extends IFeaturedObject.Builder<Group<T>> {
    Builder<T> name(String name);
    Builder<T> concrete(boolean concrete);
    Builder<T> addInclude(Supplier<Include<?>> include);
    Builder<T> addFeature(Supplier<Feature<?, ?, ?, ?>> feature);
    Builder<T> addGeneric(Supplier<Generic<?>> generic);
    Builder<T> addOperation(Supplier<Operation> operation);
    Builder<T> lmBuilder(BuilderSupplier<T> lmBuilder);
    Builder<T> addIncludes(List<Include<?>> includes);
    Builder<T> addFeatures(List<Feature<?, ?, ?, ?>> features);
    Builder<T> addGenerics(List<Generic<?>> generics);
    Builder<T> addOperations(List<Operation> operations);
  }
}
