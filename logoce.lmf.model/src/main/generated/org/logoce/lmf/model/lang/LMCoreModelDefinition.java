package org.logoce.lmf.model.lang;

import java.util.List;
import org.logoce.lmf.model.api.model.BuilderSupplier;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.builder.AliasBuilder;
import org.logoce.lmf.model.lang.builder.AttributeBuilder;
import org.logoce.lmf.model.lang.builder.EnumBuilder;
import org.logoce.lmf.model.lang.builder.GenericBuilder;
import org.logoce.lmf.model.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.model.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.model.lang.builder.GroupBuilder;
import org.logoce.lmf.model.lang.builder.IncludeBuilder;
import org.logoce.lmf.model.lang.builder.JavaWrapperBuilder;
import org.logoce.lmf.model.lang.builder.MetaModelBuilder;
import org.logoce.lmf.model.lang.builder.OperationBuilder;
import org.logoce.lmf.model.lang.builder.OperationParameterBuilder;
import org.logoce.lmf.model.lang.builder.RelationBuilder;
import org.logoce.lmf.model.lang.builder.SerializerBuilder;
import org.logoce.lmf.model.lang.builder.UnitBuilder;

public interface LMCoreModelDefinition {
  interface Generics {
    interface TYPE {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface CONCEPT {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface GROUP {
      Generic<? extends LMObject> T = new GenericBuilder<LMObject>().name("T").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreModelDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface INCLUDE {
      Generic<? extends LMObject> T = new GenericBuilder<LMObject>().name("T").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreModelDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface FEATURE {
      Generic<?> UNARY_TYPE = new GenericBuilder<>().name("UnaryType").build();
      Generic<?> EFFECTIVE_TYPE = new GenericBuilder<>().name("EffectiveType").build();
      List<Generic<?>> ALL = List.of(UNARY_TYPE, EFFECTIVE_TYPE);
    }

    interface ATTRIBUTE {
      Generic<?> UNARY_TYPE = new GenericBuilder<>().name("UnaryType").build();
      Generic<?> EFFECTIVE_TYPE = new GenericBuilder<>().name("EffectiveType").build();
      List<Generic<?>> ALL = List.of(UNARY_TYPE, EFFECTIVE_TYPE);
    }

    interface RELATION {
      Generic<? extends LMObject> UNARY_TYPE = new GenericBuilder<LMObject>().name("UnaryType").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreModelDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
      Generic<?> EFFECTIVE_TYPE = new GenericBuilder<>().name("EffectiveType").build();
      List<Generic<?>> ALL = List.of(UNARY_TYPE, EFFECTIVE_TYPE);
    }

    interface DATATYPE {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface ENUM {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface UNIT {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface GENERIC {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface JAVA_WRAPPER {
      Generic<?> T = new GenericBuilder<>().name("T").build();
      List<Generic<?>> ALL = List.of(T);
    }
  }

  interface Groups {
    Group<LMObject> LM_OBJECT = new GroupBuilder<LMObject>().name("LMObject").addFeatures(Features.LMObject.ALL).build();
    Group<Named> NAMED = new GroupBuilder<Named>().name("Named").addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Features.Named.ALL).build();
    Group<Type<?>> TYPE = new GroupBuilder<Type<?>>().name("Type").addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Features.Type.ALL).addGenerics(Generics.TYPE.ALL).build();
    Group<Model> MODEL = new GroupBuilder<Model>().name("Model").addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Features.Model.ALL).build();
    Group<MetaModel> META_MODEL = new GroupBuilder<MetaModel>().name("MetaModel").concrete(true).addInclude(() -> new IncludeBuilder<Model>().group(() -> MODEL).build()).addFeatures(Features.MetaModel.ALL).lmBuilder(new BuilderSupplier<>(MetaModelBuilder::new)).build();
    Group<Concept<?>> CONCEPT = new GroupBuilder<Concept<?>>().name("Concept").addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.CONCEPT.ALL.get(0)).build()).build()).addFeatures(Features.Concept.ALL).addGenerics(Generics.CONCEPT.ALL).build();
    Group<Group<?>> GROUP = new GroupBuilder<Group<?>>().name("Group").concrete(true).addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.ALL.get(0)).build()).build()).addInclude(() -> new IncludeBuilder<Concept<?>>().group(() -> CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.ALL.get(0)).build()).build()).addFeatures(Features.Group.ALL).addGenerics(Generics.GROUP.ALL).lmBuilder(new BuilderSupplier<>(GroupBuilder::new)).build();
    Group<Include<?>> INCLUDE = new GroupBuilder<Include<?>>().name("Include").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Features.Include.ALL).addGenerics(Generics.INCLUDE.ALL).lmBuilder(new BuilderSupplier<>(IncludeBuilder::new)).build();
    Group<Feature<?, ?>> FEATURE = new GroupBuilder<Feature<?, ?>>().name("Feature").addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Features.Feature.ALL).addGenerics(Generics.FEATURE.ALL).build();
    Group<Attribute<?, ?>> ATTRIBUTE = new GroupBuilder<Attribute<?, ?>>().name("Attribute").concrete(true).addInclude(() -> new IncludeBuilder<Feature<?, ?>>().group(() -> FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.ALL.get(0)).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.ALL.get(1)).build()).build()).addFeatures(Features.Attribute.ALL).addGenerics(Generics.ATTRIBUTE.ALL).lmBuilder(new BuilderSupplier<>(AttributeBuilder::new)).build();
    Group<Relation<?, ?>> RELATION = new GroupBuilder<Relation<?, ?>>().name("Relation").concrete(true).addInclude(() -> new IncludeBuilder<Feature<?, ?>>().group(() -> FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.ALL.get(0)).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.ALL.get(1)).build()).build()).addFeatures(Features.Relation.ALL).addGenerics(Generics.RELATION.ALL).lmBuilder(new BuilderSupplier<>(RelationBuilder::new)).build();
    Group<Operation> OPERATION = new GroupBuilder<Operation>().name("Operation").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Features.Operation.ALL).lmBuilder(new BuilderSupplier<>(OperationBuilder::new)).build();
    Group<OperationParameter> OPERATION_PARAMETER = new GroupBuilder<OperationParameter>().name("OperationParameter").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Features.OperationParameter.ALL).lmBuilder(new BuilderSupplier<>(OperationParameterBuilder::new)).build();
    Group<Datatype<?>> DATATYPE = new GroupBuilder<Datatype<?>>().name("Datatype").addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.DATATYPE.ALL.get(0)).build()).build()).addFeatures(Features.Datatype.ALL).addGenerics(Generics.DATATYPE.ALL).build();
    Group<Alias> ALIAS = new GroupBuilder<Alias>().name("Alias").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Features.Alias.ALL).lmBuilder(new BuilderSupplier<>(AliasBuilder::new)).build();
    Group<Enum<?>> ENUM = new GroupBuilder<Enum<?>>().name("Enum").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ENUM.ALL.get(0)).build()).build()).addFeatures(Features.Enum.ALL).addGenerics(Generics.ENUM.ALL).lmBuilder(new BuilderSupplier<>(EnumBuilder::new)).build();
    Group<Unit<?>> UNIT = new GroupBuilder<Unit<?>>().name("Unit").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.UNIT.ALL.get(0)).build()).build()).addFeatures(Features.Unit.ALL).addGenerics(Generics.UNIT.ALL).lmBuilder(new BuilderSupplier<>(UnitBuilder::new)).build();
    Group<Generic<?>> GENERIC = new GroupBuilder<Generic<?>>().name("Generic").concrete(true).addInclude(() -> new IncludeBuilder<Concept<?>>().group(() -> CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GENERIC.ALL.get(0)).build()).build()).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GENERIC.ALL.get(0)).build()).build()).addFeatures(Features.Generic.ALL).addGenerics(Generics.GENERIC.ALL).lmBuilder(new BuilderSupplier<>(GenericBuilder::new)).build();
    Group<GenericExtension> GENERIC_EXTENSION = new GroupBuilder<GenericExtension>().name("GenericExtension").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Features.GenericExtension.ALL).lmBuilder(new BuilderSupplier<>(GenericExtensionBuilder::new)).build();
    Group<GenericParameter> GENERIC_PARAMETER = new GroupBuilder<GenericParameter>().name("GenericParameter").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Features.GenericParameter.ALL).lmBuilder(new BuilderSupplier<>(GenericParameterBuilder::new)).build();
    Group<JavaWrapper<?>> JAVA_WRAPPER = new GroupBuilder<JavaWrapper<?>>().name("JavaWrapper").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.JAVA_WRAPPER.ALL.get(0)).build()).build()).addFeatures(Features.JavaWrapper.ALL).addGenerics(Generics.JAVA_WRAPPER.ALL).lmBuilder(new BuilderSupplier<>(JavaWrapperBuilder::new)).build();
    Group<Serializer> SERIALIZER = new GroupBuilder<Serializer>().name("Serializer").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Features.Serializer.ALL).lmBuilder(new BuilderSupplier<>(SerializerBuilder::new)).build();
    List<Group<?>> ALL = List.of(LM_OBJECT, NAMED, TYPE, MODEL, META_MODEL, CONCEPT, GROUP, INCLUDE, FEATURE, ATTRIBUTE, RELATION, OPERATION, OPERATION_PARAMETER, DATATYPE, ALIAS, ENUM, UNIT, GENERIC, GENERIC_EXTENSION, GENERIC_PARAMETER, JAVA_WRAPPER, SERIALIZER);
  }

  interface Features {
    interface LMObject {
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of();
    }

    interface Named {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = new AttributeBuilder<String, String>().name("name").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.Named.FeatureIDs.NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME);
    }

    interface Type {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME);
    }

    interface Model {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, String> DOMAIN = new AttributeBuilder<String, String>().name("domain").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.Model.FeatureIDs.DOMAIN).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Attribute<String, List<String>> IMPORTS = new AttributeBuilder<String, List<String>>().name("imports").immutable(true).many(true).id(org.logoce.lmf.model.lang.Model.FeatureIDs.IMPORTS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Attribute<String, List<String>> METAMODELS = new AttributeBuilder<String, List<String>>().name("metamodels").immutable(true).many(true).id(org.logoce.lmf.model.lang.Model.FeatureIDs.METAMODELS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, METAMODELS);
    }

    interface MetaModel {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, String> DOMAIN = Model.DOMAIN;
      org.logoce.lmf.model.lang.Attribute<String, List<String>> IMPORTS = Model.IMPORTS;
      org.logoce.lmf.model.lang.Attribute<String, List<String>> METAMODELS = Model.METAMODELS;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Group<?>, List<org.logoce.lmf.model.lang.Group<?>>> GROUPS = new RelationBuilder<org.logoce.lmf.model.lang.Group<?>, List<org.logoce.lmf.model.lang.Group<?>>>().name("groups").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.GROUPS).concept(() -> LMCoreModelDefinition.Groups.GROUP).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Enum<?>, List<org.logoce.lmf.model.lang.Enum<?>>> ENUMS = new RelationBuilder<org.logoce.lmf.model.lang.Enum<?>, List<org.logoce.lmf.model.lang.Enum<?>>>().name("enums").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.ENUMS).concept(() -> LMCoreModelDefinition.Groups.ENUM).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Unit<?>, List<org.logoce.lmf.model.lang.Unit<?>>> UNITS = new RelationBuilder<org.logoce.lmf.model.lang.Unit<?>, List<org.logoce.lmf.model.lang.Unit<?>>>().name("units").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.UNITS).concept(() -> LMCoreModelDefinition.Groups.UNIT).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Alias, List<org.logoce.lmf.model.lang.Alias>> ALIASES = new RelationBuilder<org.logoce.lmf.model.lang.Alias, List<org.logoce.lmf.model.lang.Alias>>().name("aliases").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.ALIASES).concept(() -> LMCoreModelDefinition.Groups.ALIAS).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.JavaWrapper<?>, List<org.logoce.lmf.model.lang.JavaWrapper<?>>> JAVA_WRAPPERS = new RelationBuilder<org.logoce.lmf.model.lang.JavaWrapper<?>, List<org.logoce.lmf.model.lang.JavaWrapper<?>>>().name("javaWrappers").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.JAVA_WRAPPERS).concept(() -> LMCoreModelDefinition.Groups.JAVA_WRAPPER).build();
      org.logoce.lmf.model.lang.Attribute<IModelPackage, IModelPackage> LM_PACKAGE = new AttributeBuilder<IModelPackage, IModelPackage>().name("lmPackage").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.LM_PACKAGE).datatype(() -> LMCoreModelDefinition.JavaWrappers.I_MODEL_PACKAGE).build();
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> GEN_NAME_PACKAGE = new AttributeBuilder<Boolean, Boolean>().name("genNamePackage").immutable(true).defaultValue("true").id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.GEN_NAME_PACKAGE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Attribute<String, String> EXTRA_PACKAGE = new AttributeBuilder<String, String>().name("extraPackage").immutable(true).id(org.logoce.lmf.model.lang.MetaModel.FeatureIDs.EXTRA_PACKAGE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, METAMODELS, GROUPS, ENUMS, UNITS, ALIASES, JAVA_WRAPPERS, LM_PACKAGE, GEN_NAME_PACKAGE, EXTRA_PACKAGE);
    }

    interface Concept {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME);
    }

    interface Group {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> CONCRETE = new AttributeBuilder<Boolean, Boolean>().name("concrete").immutable(true).id(org.logoce.lmf.model.lang.Group.FeatureIDs.CONCRETE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Include<?>, List<org.logoce.lmf.model.lang.Include<?>>> INCLUDES = new RelationBuilder<org.logoce.lmf.model.lang.Include<?>, List<org.logoce.lmf.model.lang.Include<?>>>().name("includes").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Group.FeatureIDs.INCLUDES).concept(() -> LMCoreModelDefinition.Groups.INCLUDE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Feature<?, ?>, List<org.logoce.lmf.model.lang.Feature<?, ?>>> FEATURES = new RelationBuilder<org.logoce.lmf.model.lang.Feature<?, ?>, List<org.logoce.lmf.model.lang.Feature<?, ?>>>().name("features").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Group.FeatureIDs.FEATURES).concept(() -> LMCoreModelDefinition.Groups.FEATURE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Generic<?>, List<org.logoce.lmf.model.lang.Generic<?>>> GENERICS = new RelationBuilder<org.logoce.lmf.model.lang.Generic<?>, List<org.logoce.lmf.model.lang.Generic<?>>>().name("generics").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Group.FeatureIDs.GENERICS).concept(() -> LMCoreModelDefinition.Groups.GENERIC).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Operation, List<org.logoce.lmf.model.lang.Operation>> OPERATIONS = new RelationBuilder<org.logoce.lmf.model.lang.Operation, List<org.logoce.lmf.model.lang.Operation>>().name("operations").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Group.FeatureIDs.OPERATIONS).concept(() -> LMCoreModelDefinition.Groups.OPERATION).build();
      org.logoce.lmf.model.lang.Attribute<BuilderSupplier<?>, BuilderSupplier<?>> LM_BUILDER = new AttributeBuilder<BuilderSupplier<?>, BuilderSupplier<?>>().name("lmBuilder").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.Group.FeatureIDs.LM_BUILDER).datatype(() -> LMCoreModelDefinition.JavaWrappers.BUILDER_SUPPLIER).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.ALL.get(0)).build()).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, CONCRETE, INCLUDES, FEATURES, GENERICS, OPERATIONS, LM_BUILDER);
    }

    interface Include {
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Group<?>, org.logoce.lmf.model.lang.Group<?>> GROUP = new RelationBuilder<org.logoce.lmf.model.lang.Group<?>, org.logoce.lmf.model.lang.Group<?>>().name("group").immutable(true).mandatory(true).lazy(true).id(org.logoce.lmf.model.lang.Include.FeatureIDs.GROUP).concept(() -> LMCoreModelDefinition.Groups.GROUP).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.INCLUDE.ALL.get(0)).build()).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Include.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(GROUP, PARAMETERS);
    }

    interface Feature {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> IMMUTABLE = new AttributeBuilder<Boolean, Boolean>().name("immutable").immutable(true).id(org.logoce.lmf.model.lang.Feature.FeatureIDs.IMMUTABLE).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Attribute<Integer, Integer> ID = new AttributeBuilder<Integer, Integer>().name("id").immutable(true).id(org.logoce.lmf.model.lang.Feature.FeatureIDs.ID).datatype(() -> LMCoreModelDefinition.Units.INT).build();
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> MANY = new AttributeBuilder<Boolean, Boolean>().name("many").immutable(true).id(org.logoce.lmf.model.lang.Feature.FeatureIDs.MANY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> MANDATORY = new AttributeBuilder<Boolean, Boolean>().name("mandatory").immutable(true).id(org.logoce.lmf.model.lang.Feature.FeatureIDs.MANDATORY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Feature.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS);
    }

    interface Attribute {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> IMMUTABLE = Feature.IMMUTABLE;
      org.logoce.lmf.model.lang.Attribute<Integer, Integer> ID = Feature.ID;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> MANY = Feature.MANY;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> MANDATORY = Feature.MANDATORY;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = Feature.PARAMETERS;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Datatype<?>, org.logoce.lmf.model.lang.Datatype<?>> DATATYPE = new RelationBuilder<org.logoce.lmf.model.lang.Datatype<?>, org.logoce.lmf.model.lang.Datatype<?>>().name("datatype").immutable(true).mandatory(true).lazy(true).id(org.logoce.lmf.model.lang.Attribute.FeatureIDs.DATATYPE).concept(() -> LMCoreModelDefinition.Groups.DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.ALL.get(0)).build()).build();
      org.logoce.lmf.model.lang.Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).id(org.logoce.lmf.model.lang.Attribute.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, DATATYPE, DEFAULT_VALUE);
    }

    interface Relation {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> IMMUTABLE = Feature.IMMUTABLE;
      org.logoce.lmf.model.lang.Attribute<Integer, Integer> ID = Feature.ID;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> MANY = Feature.MANY;
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> MANDATORY = Feature.MANDATORY;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = Feature.PARAMETERS;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Concept<?>, org.logoce.lmf.model.lang.Concept<?>> CONCEPT = new RelationBuilder<org.logoce.lmf.model.lang.Concept<?>, org.logoce.lmf.model.lang.Concept<?>>().name("concept").immutable(true).mandatory(true).lazy(true).id(org.logoce.lmf.model.lang.Relation.FeatureIDs.CONCEPT).concept(() -> LMCoreModelDefinition.Groups.CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.ALL.get(0)).build()).build();
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> LAZY = new AttributeBuilder<Boolean, Boolean>().name("lazy").immutable(true).id(org.logoce.lmf.model.lang.Relation.FeatureIDs.LAZY).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> CONTAINS = new AttributeBuilder<Boolean, Boolean>().name("contains").immutable(true).id(org.logoce.lmf.model.lang.Relation.FeatureIDs.CONTAINS).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, ID, MANY, MANDATORY, PARAMETERS, CONCEPT, LAZY, CONTAINS);
    }

    interface Operation {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, String> CONTENT = new AttributeBuilder<String, String>().name("content").immutable(true).id(org.logoce.lmf.model.lang.Operation.FeatureIDs.CONTENT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>> RETURN_TYPE = new RelationBuilder<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>>().name("returnType").immutable(true).lazy(true).id(org.logoce.lmf.model.lang.Operation.FeatureIDs.RETURN_TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> RETURN_TYPE_PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>>().name("returnTypeParameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Operation.FeatureIDs.RETURN_TYPE_PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.OperationParameter, List<org.logoce.lmf.model.lang.OperationParameter>> PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.OperationParameter, List<org.logoce.lmf.model.lang.OperationParameter>>().name("parameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.Operation.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.OPERATION_PARAMETER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, CONTENT, RETURN_TYPE, RETURN_TYPE_PARAMETERS, PARAMETERS);
    }

    interface OperationParameter {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>> TYPE = new RelationBuilder<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>>().name("type").immutable(true).mandatory(true).lazy(true).id(org.logoce.lmf.model.lang.OperationParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.OperationParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, TYPE, PARAMETERS);
    }

    interface Datatype {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME);
    }

    interface Alias {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, String> VALUE = new AttributeBuilder<String, String>().name("value").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.Alias.FeatureIDs.VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, VALUE);
    }

    interface Enum {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, List<String>> LITERALS = new AttributeBuilder<String, List<String>>().name("literals").immutable(true).many(true).id(org.logoce.lmf.model.lang.Enum.FeatureIDs.LITERALS).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, LITERALS);
    }

    interface Unit {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, String> MATCHER = new AttributeBuilder<String, String>().name("matcher").immutable(true).id(org.logoce.lmf.model.lang.Unit.FeatureIDs.MATCHER).datatype(() -> LMCoreModelDefinition.Units.MATCHER).build();
      org.logoce.lmf.model.lang.Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).id(org.logoce.lmf.model.lang.Unit.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Attribute<Primitive, Primitive> PRIMITIVE = new AttributeBuilder<Primitive, Primitive>().name("primitive").immutable(true).defaultValue("String").id(org.logoce.lmf.model.lang.Unit.FeatureIDs.PRIMITIVE).datatype(() -> LMCoreModelDefinition.Enums.PRIMITIVE).build();
      org.logoce.lmf.model.lang.Attribute<String, String> EXTRACTOR = new AttributeBuilder<String, String>().name("extractor").immutable(true).id(org.logoce.lmf.model.lang.Unit.FeatureIDs.EXTRACTOR).datatype(() -> LMCoreModelDefinition.Units.EXTRACTOR).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, MATCHER, DEFAULT_VALUE, PRIMITIVE, EXTRACTOR);
    }

    interface Generic {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericExtension, org.logoce.lmf.model.lang.GenericExtension> EXTENSION = new RelationBuilder<org.logoce.lmf.model.lang.GenericExtension, org.logoce.lmf.model.lang.GenericExtension>().name("extension").immutable(true).contains(true).id(org.logoce.lmf.model.lang.Generic.FeatureIDs.EXTENSION).concept(() -> LMCoreModelDefinition.Groups.GENERIC_EXTENSION).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, EXTENSION);
    }

    interface GenericExtension {
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>> TYPE = new RelationBuilder<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>>().name("type").immutable(true).lazy(true).id(org.logoce.lmf.model.lang.GenericExtension.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
      org.logoce.lmf.model.lang.Attribute<BoundType, BoundType> BOUND_TYPE = new AttributeBuilder<BoundType, BoundType>().name("boundType").immutable(true).id(org.logoce.lmf.model.lang.GenericExtension.FeatureIDs.BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.GenericExtension.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(TYPE, BOUND_TYPE, PARAMETERS);
    }

    interface GenericParameter {
      org.logoce.lmf.model.lang.Attribute<Boolean, Boolean> WILDCARD = new AttributeBuilder<Boolean, Boolean>().name("wildcard").immutable(true).id(org.logoce.lmf.model.lang.GenericParameter.FeatureIDs.WILDCARD).datatype(() -> LMCoreModelDefinition.Units.BOOLEAN).build();
      org.logoce.lmf.model.lang.Attribute<BoundType, BoundType> WILDCARD_BOUND_TYPE = new AttributeBuilder<BoundType, BoundType>().name("wildcardBoundType").immutable(true).id(org.logoce.lmf.model.lang.GenericParameter.FeatureIDs.WILDCARD_BOUND_TYPE).datatype(() -> LMCoreModelDefinition.Enums.BOUND_TYPE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>> TYPE = new RelationBuilder<org.logoce.lmf.model.lang.Type<?>, org.logoce.lmf.model.lang.Type<?>>().name("type").immutable(true).mandatory(true).lazy(true).id(org.logoce.lmf.model.lang.GenericParameter.FeatureIDs.TYPE).concept(() -> LMCoreModelDefinition.Groups.TYPE).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>> PARAMETERS = new RelationBuilder<org.logoce.lmf.model.lang.GenericParameter, List<org.logoce.lmf.model.lang.GenericParameter>>().name("parameters").immutable(true).many(true).contains(true).id(org.logoce.lmf.model.lang.GenericParameter.FeatureIDs.PARAMETERS).concept(() -> LMCoreModelDefinition.Groups.GENERIC_PARAMETER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(WILDCARD, WILDCARD_BOUND_TYPE, TYPE, PARAMETERS);
    }

    interface JavaWrapper {
      org.logoce.lmf.model.lang.Attribute<String, String> NAME = Named.NAME;
      org.logoce.lmf.model.lang.Attribute<String, String> QUALIFIED_CLASS_NAME = new AttributeBuilder<String, String>().name("qualifiedClassName").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.JavaWrapper.FeatureIDs.QUALIFIED_CLASS_NAME).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Relation<org.logoce.lmf.model.lang.Serializer, org.logoce.lmf.model.lang.Serializer> SERIALIZER = new RelationBuilder<org.logoce.lmf.model.lang.Serializer, org.logoce.lmf.model.lang.Serializer>().name("serializer").immutable(true).contains(true).id(org.logoce.lmf.model.lang.JavaWrapper.FeatureIDs.SERIALIZER).concept(() -> LMCoreModelDefinition.Groups.SERIALIZER).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(NAME, QUALIFIED_CLASS_NAME, SERIALIZER);
    }

    interface Serializer {
      org.logoce.lmf.model.lang.Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).id(org.logoce.lmf.model.lang.Serializer.FeatureIDs.DEFAULT_VALUE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Attribute<String, String> CREATE = new AttributeBuilder<String, String>().name("create").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.Serializer.FeatureIDs.CREATE).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      org.logoce.lmf.model.lang.Attribute<String, String> CONVERT = new AttributeBuilder<String, String>().name("convert").immutable(true).mandatory(true).id(org.logoce.lmf.model.lang.Serializer.FeatureIDs.CONVERT).datatype(() -> LMCoreModelDefinition.Units.STRING).build();
      List<org.logoce.lmf.model.lang.Feature<?, ?>> ALL = List.of(DEFAULT_VALUE, CREATE, CONVERT);
    }
  }

  interface Units {
    Unit<String> MATCHER = new UnitBuilder<String>().name("matcher").matcher("rgx_match:<(.+?)>").primitive(Primitive.String).build();
    Unit<String> EXTRACTOR = new UnitBuilder<String>().name("extractor").matcher("rgx_match:<(.+?)>").primitive(Primitive.String).build();
    Unit<Boolean> BOOLEAN = new UnitBuilder<Boolean>().name("boolean").matcher("rgx_match:<(true|false)>").defaultValue("false").primitive(Primitive.Boolean).build();
    Unit<Integer> INT = new UnitBuilder<Integer>().name("int").matcher("rgx_match:<[0-9]+>").defaultValue("0").primitive(Primitive.Int).build();
    Unit<Long> LONG = new UnitBuilder<Long>().name("long").matcher("rgx_match:<[0-9]+[Ll]>").defaultValue("0L").primitive(Primitive.Long).build();
    Unit<Float> FLOAT = new UnitBuilder<Float>().name("float").matcher("rgx_match:<[0-9.]+[Ff]>").defaultValue("0f").primitive(Primitive.Float).build();
    Unit<Double> DOUBLE = new UnitBuilder<Double>().name("double").matcher("rgx_match:<[0-9.]+>").defaultValue("0.").primitive(Primitive.Double).build();
    Unit<String> STRING = new UnitBuilder<String>().name("string").defaultValue("").primitive(Primitive.String).build();
    List<Unit<?>> ALL = List.of(MATCHER, EXTRACTOR, BOOLEAN, INT, LONG, FLOAT, DOUBLE, STRING);
  }

  interface Enums {
    Enum<BoundType> BOUND_TYPE = new EnumBuilder<BoundType>().name("BoundType").addLiterals(List.of("extends", "super")).build();
    Enum<Primitive> PRIMITIVE = new EnumBuilder<Primitive>().name("Primitive").addLiterals(List.of("boolean", "int", "long", "float", "double", "string")).build();
    List<Enum<?>> ALL = List.of(BOUND_TYPE, PRIMITIVE);
  }

  interface Aliases {
    Alias DEFINITION = new AliasBuilder().name("Definition").value("Group concrete").build();
    Alias PLUS_CONTAINS = new AliasBuilder().name("+contains").value("Relation contains immutable=false").build();
    Alias MINUS_CONTAINS = new AliasBuilder().name("-contains").value("Relation contains immutable").build();
    Alias PLUS_REFERS = new AliasBuilder().name("+refers").value("Relation contains=false immutable=false").build();
    Alias MINUS_REFERS = new AliasBuilder().name("-refers").value("Relation contains=false immutable").build();
    Alias PLUS_ATT = new AliasBuilder().name("+att").value("Attribute immutable=false").build();
    Alias MINUS_ATT = new AliasBuilder().name("-att").value("Attribute immutable").build();
    Alias LSB_0_DOT_DOT_1_RSB = new AliasBuilder().name("[0..1]").value("mandatory=false many=false").build();
    Alias LSB_1_DOT_DOT_1_RSB = new AliasBuilder().name("[1..1]").value("mandatory many=false").build();
    Alias LSB_0_DOT_DOT_STAR_RSB = new AliasBuilder().name("[0..*]").value("mandatory=false many").build();
    Alias LSB_1_DOT_DOT_STAR_RSB = new AliasBuilder().name("[1..*]").value("mandatory many").build();
    List<Alias> ALL = List.of(DEFINITION, PLUS_CONTAINS, MINUS_CONTAINS, PLUS_REFERS, MINUS_REFERS, PLUS_ATT, MINUS_ATT, LSB_0_DOT_DOT_1_RSB, LSB_1_DOT_DOT_1_RSB, LSB_0_DOT_DOT_STAR_RSB, LSB_1_DOT_DOT_STAR_RSB);
  }

  interface JavaWrappers {
    JavaWrapper<IModelPackage> I_MODEL_PACKAGE = new JavaWrapperBuilder<IModelPackage>().name("IModelPackage").qualifiedClassName("org.logoce.lmf.model.api.model.IModelPackage").build();
    JavaWrapper<BuilderSupplier<?>> BUILDER_SUPPLIER = new JavaWrapperBuilder<BuilderSupplier<?>>().name("BuilderSupplier").qualifiedClassName("org.logoce.lmf.model.api.model.BuilderSupplier").build();
    List<JavaWrapper<?>> ALL = List.of(I_MODEL_PACKAGE, BUILDER_SUPPLIER);
  }
}
