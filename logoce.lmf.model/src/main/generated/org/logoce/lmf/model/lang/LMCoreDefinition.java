package org.logoce.lmf.model.lang;

import java.lang.Boolean;
import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
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

public interface LMCoreDefinition {
  interface Features {
    interface LM_OBJECT {
      List<Feature<?, ?>> ALL = List.of();
    }

    interface NAMED {
      Attribute<String, String> NAME = new AttributeBuilder<String, String>().name("name").immutable(true).many(false).mandatory(true).rawFeature(Named.Features.name).datatype(() -> Units.STRING).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface TYPE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface MODEL {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, String> DOMAIN = new AttributeBuilder<String, String>().name("domain").immutable(true).many(false).mandatory(true).rawFeature(Model.Features.domain).datatype(() -> Units.STRING).defaultValue(null).build();
      Attribute<String, List<String>> IMPORTS = new AttributeBuilder<String, List<String>>().name("imports").immutable(true).many(true).mandatory(false).rawFeature(Model.Features.imports).datatype(() -> Units.STRING).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS);
    }

    interface META_MODEL {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, String> DOMAIN = LMCoreDefinition.Features.MODEL.DOMAIN;
      Attribute<String, List<String>> IMPORTS = LMCoreDefinition.Features.MODEL.IMPORTS;
      Relation<Group<?>, List<Group<?>>> GROUPS = new RelationBuilder<Group<?>, List<Group<?>>>().name("groups").immutable(true).many(true).mandatory(false).rawFeature(MetaModel.Features.groups).concept(() -> LMCoreDefinition.Groups.GROUP).lazy(false).contains(true).build();
      Relation<Enum<?>, List<Enum<?>>> ENUMS = new RelationBuilder<Enum<?>, List<Enum<?>>>().name("enums").immutable(true).many(true).mandatory(false).rawFeature(MetaModel.Features.enums).concept(() -> LMCoreDefinition.Groups.ENUM).lazy(false).contains(true).build();
      Relation<Unit<?>, List<Unit<?>>> UNITS = new RelationBuilder<Unit<?>, List<Unit<?>>>().name("units").immutable(true).many(true).mandatory(false).rawFeature(MetaModel.Features.units).concept(() -> LMCoreDefinition.Groups.UNIT).lazy(false).contains(true).build();
      Relation<Alias, List<Alias>> ALIASES = new RelationBuilder<Alias, List<Alias>>().name("aliases").immutable(true).many(true).mandatory(false).rawFeature(MetaModel.Features.aliases).concept(() -> LMCoreDefinition.Groups.ALIAS).lazy(false).contains(true).build();
      Relation<JavaWrapper<?>, List<JavaWrapper<?>>> JAVA_WRAPPERS = new RelationBuilder<JavaWrapper<?>, List<JavaWrapper<?>>>().name("javaWrappers").immutable(true).many(true).mandatory(false).rawFeature(MetaModel.Features.javaWrappers).concept(() -> LMCoreDefinition.Groups.JAVA_WRAPPER).lazy(false).contains(true).build();
      Attribute<IModelPackage, IModelPackage> LM_PACKAGE = new AttributeBuilder<IModelPackage, IModelPackage>().name("lmPackage").immutable(true).many(false).mandatory(true).rawFeature(MetaModel.Features.lmPackage).datatype(() -> JavaWrappers.I_MODEL_PACKAGE).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, GROUPS, ENUMS, UNITS, ALIASES, JAVA_WRAPPERS, LM_PACKAGE);
    }

    interface CONCEPT {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface GROUP {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<Boolean, Boolean> CONCRETE = new AttributeBuilder<Boolean, Boolean>().name("concrete").immutable(true).many(false).mandatory(false).rawFeature(Group.Features.concrete).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      Relation<Include<?>, List<Include<?>>> INCLUDES = new RelationBuilder<Include<?>, List<Include<?>>>().name("includes").immutable(true).many(true).mandatory(false).rawFeature(Group.Features.includes).concept(() -> LMCoreDefinition.Groups.INCLUDE).lazy(false).contains(true).build();
      Relation<Feature<?, ?>, List<Feature<?, ?>>> FEATURES = new RelationBuilder<Feature<?, ?>, List<Feature<?, ?>>>().name("features").immutable(true).many(true).mandatory(false).rawFeature(Group.Features.features).concept(() -> LMCoreDefinition.Groups.FEATURE).lazy(false).contains(true).build();
      Relation<Generic<?>, List<Generic<?>>> GENERICS = new RelationBuilder<Generic<?>, List<Generic<?>>>().name("generics").immutable(true).many(true).mandatory(false).rawFeature(Group.Features.generics).concept(() -> LMCoreDefinition.Groups.GENERIC).lazy(false).contains(true).build();
      Relation<Operation, List<Operation>> OPERATIONS = new RelationBuilder<Operation, List<Operation>>().name("operations").immutable(true).many(true).mandatory(false).rawFeature(Group.Features.operations).concept(() -> LMCoreDefinition.Groups.OPERATION).lazy(false).contains(true).build();
      Attribute<BuilderSupplier<?>, BuilderSupplier<?>> LM_BUILDER = new AttributeBuilder<BuilderSupplier<?>, BuilderSupplier<?>>().name("lmBuilder").immutable(true).many(false).mandatory(true).rawFeature(Group.Features.lmBuilder).datatype(() -> JavaWrappers.BUILDER_SUPPLIER).defaultValue(null).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.GROUP.ALL.get(0)).build()).build();
      List<Feature<?, ?>> ALL = List.of(NAME, CONCRETE, INCLUDES, FEATURES, GENERICS, OPERATIONS, LM_BUILDER);
    }

    interface INCLUDE {
      Relation<Group<?>, Group<?>> GROUP = new RelationBuilder<Group<?>, Group<?>>().name("group").immutable(true).many(false).mandatory(true).rawFeature(Include.Features.group).concept(() -> LMCoreDefinition.Groups.GROUP).lazy(true).contains(false).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.INCLUDE.ALL.get(0)).build()).build();
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).mandatory(false).rawFeature(Include.Features.parameters).concept(() -> LMCoreDefinition.Groups.GENERIC_PARAMETER).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(GROUP, PARAMETERS);
    }

    interface FEATURE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<Boolean, Boolean> IMMUTABLE = new AttributeBuilder<Boolean, Boolean>().name("immutable").immutable(true).many(false).mandatory(false).rawFeature(Feature.Features.immutable).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      Attribute<Boolean, Boolean> MANY = new AttributeBuilder<Boolean, Boolean>().name("many").immutable(true).many(false).mandatory(false).rawFeature(Feature.Features.many).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      Attribute<Boolean, Boolean> MANDATORY = new AttributeBuilder<Boolean, Boolean>().name("mandatory").immutable(true).many(false).mandatory(false).rawFeature(Feature.Features.mandatory).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).mandatory(false).rawFeature(Feature.Features.parameters).concept(() -> LMCoreDefinition.Groups.GENERIC_PARAMETER).lazy(false).contains(true).build();
      Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = new AttributeBuilder<RawFeature<?, ?>, RawFeature<?, ?>>().name("rawFeature").immutable(true).many(false).mandatory(false).rawFeature(Feature.Features.rawFeature).datatype(() -> JavaWrappers.RAW_FEATURE).defaultValue(null).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.FEATURE.ALL.get(0)).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.FEATURE.ALL.get(1)).build()).build();
      List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, PARAMETERS, RAW_FEATURE);
    }

    interface ATTRIBUTE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<Boolean, Boolean> IMMUTABLE = LMCoreDefinition.Features.FEATURE.IMMUTABLE;
      Attribute<Boolean, Boolean> MANY = LMCoreDefinition.Features.FEATURE.MANY;
      Attribute<Boolean, Boolean> MANDATORY = LMCoreDefinition.Features.FEATURE.MANDATORY;
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = LMCoreDefinition.Features.FEATURE.PARAMETERS;
      Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = LMCoreDefinition.Features.FEATURE.RAW_FEATURE;
      Relation<Datatype<?>, Datatype<?>> DATATYPE = new RelationBuilder<Datatype<?>, Datatype<?>>().name("datatype").immutable(true).many(false).mandatory(true).rawFeature(Attribute.Features.datatype).concept(() -> LMCoreDefinition.Groups.DATATYPE).lazy(false).contains(false).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.ATTRIBUTE.ALL.get(0)).build()).build();
      Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).many(false).mandatory(false).rawFeature(Attribute.Features.defaultValue).datatype(() -> Units.STRING).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, PARAMETERS, RAW_FEATURE, DATATYPE, DEFAULT_VALUE);
    }

    interface RELATION {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<Boolean, Boolean> IMMUTABLE = LMCoreDefinition.Features.FEATURE.IMMUTABLE;
      Attribute<Boolean, Boolean> MANY = LMCoreDefinition.Features.FEATURE.MANY;
      Attribute<Boolean, Boolean> MANDATORY = LMCoreDefinition.Features.FEATURE.MANDATORY;
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = LMCoreDefinition.Features.FEATURE.PARAMETERS;
      Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = LMCoreDefinition.Features.FEATURE.RAW_FEATURE;
      Relation<Concept<?>, Concept<?>> CONCEPT = new RelationBuilder<Concept<?>, Concept<?>>().name("concept").immutable(true).many(false).mandatory(true).rawFeature(Relation.Features.concept).concept(() -> LMCoreDefinition.Groups.CONCEPT).lazy(true).contains(false).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.RELATION.ALL.get(0)).build()).build();
      Attribute<Boolean, Boolean> LAZY = new AttributeBuilder<Boolean, Boolean>().name("lazy").immutable(true).many(false).mandatory(false).rawFeature(Relation.Features.lazy).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      Attribute<Boolean, Boolean> CONTAINS = new AttributeBuilder<Boolean, Boolean>().name("contains").immutable(true).many(false).mandatory(false).rawFeature(Relation.Features.contains).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, PARAMETERS, RAW_FEATURE, CONCEPT, LAZY, CONTAINS);
    }

    interface OPERATION {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, String> CONTENT = new AttributeBuilder<String, String>().name("content").immutable(true).many(false).mandatory(false).rawFeature(Operation.Features.content).datatype(() -> Units.STRING).defaultValue(null).build();
      Relation<Type<?>, Type<?>> RETURN_TYPE = new RelationBuilder<Type<?>, Type<?>>().name("returnType").immutable(true).many(false).mandatory(false).rawFeature(Operation.Features.returnType).concept(() -> LMCoreDefinition.Groups.TYPE).lazy(false).contains(false).build();
      Relation<GenericParameter, List<GenericParameter>> RETURN_TYPE_PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("returnTypeParameters").immutable(true).many(true).mandatory(false).rawFeature(Operation.Features.returnTypeParameters).concept(() -> LMCoreDefinition.Groups.GENERIC_PARAMETER).lazy(false).contains(true).build();
      Relation<OperationParameter, List<OperationParameter>> PARAMETERS = new RelationBuilder<OperationParameter, List<OperationParameter>>().name("parameters").immutable(true).many(true).mandatory(false).rawFeature(Operation.Features.parameters).concept(() -> LMCoreDefinition.Groups.OPERATION_PARAMETER).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(NAME, CONTENT, RETURN_TYPE, RETURN_TYPE_PARAMETERS, PARAMETERS);
    }

    interface OPERATION_PARAMETER {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Relation<Type<?>, Type<?>> TYPE = new RelationBuilder<Type<?>, Type<?>>().name("type").immutable(true).many(false).mandatory(true).rawFeature(OperationParameter.Features.type).concept(() -> LMCoreDefinition.Groups.TYPE).lazy(false).contains(false).build();
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).mandatory(false).rawFeature(OperationParameter.Features.parameters).concept(() -> LMCoreDefinition.Groups.GENERIC_PARAMETER).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(NAME, TYPE, PARAMETERS);
    }

    interface DATATYPE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface ALIAS {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, String> VALUE = new AttributeBuilder<String, String>().name("value").immutable(true).many(false).mandatory(true).rawFeature(Alias.Features.value).datatype(() -> Units.STRING).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, VALUE);
    }

    interface ENUM {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, List<String>> LITERALS = new AttributeBuilder<String, List<String>>().name("literals").immutable(true).many(true).mandatory(false).rawFeature(Enum.Features.literals).datatype(() -> Units.STRING).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, LITERALS);
    }

    interface UNIT {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, String> MATCHER = new AttributeBuilder<String, String>().name("matcher").immutable(true).many(false).mandatory(false).rawFeature(Unit.Features.matcher).datatype(() -> Units.MATCHER).defaultValue(null).build();
      Attribute<String, String> DEFAULT_VALUE = new AttributeBuilder<String, String>().name("defaultValue").immutable(true).many(false).mandatory(false).rawFeature(Unit.Features.defaultValue).datatype(() -> Units.STRING).defaultValue(null).build();
      Attribute<Primitive, Primitive> PRIMITIVE = new AttributeBuilder<Primitive, Primitive>().name("primitive").immutable(true).many(false).mandatory(false).rawFeature(Unit.Features.primitive).datatype(() -> Enums.PRIMITIVE).defaultValue("String").build();
      Attribute<String, String> EXTRACTOR = new AttributeBuilder<String, String>().name("extractor").immutable(true).many(false).mandatory(false).rawFeature(Unit.Features.extractor).datatype(() -> Units.EXTRACTOR).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(NAME, MATCHER, DEFAULT_VALUE, PRIMITIVE, EXTRACTOR);
    }

    interface GENERIC {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Relation<GenericExtension, GenericExtension> EXTENSION = new RelationBuilder<GenericExtension, GenericExtension>().name("extension").immutable(true).many(false).mandatory(false).rawFeature(Generic.Features.extension).concept(() -> LMCoreDefinition.Groups.GENERIC_EXTENSION).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(NAME, EXTENSION);
    }

    interface GENERIC_EXTENSION {
      Relation<Type<?>, Type<?>> TYPE = new RelationBuilder<Type<?>, Type<?>>().name("type").immutable(true).many(false).mandatory(false).rawFeature(GenericExtension.Features.type).concept(() -> LMCoreDefinition.Groups.TYPE).lazy(true).contains(false).build();
      Attribute<BoundType, BoundType> BOUND_TYPE = new AttributeBuilder<BoundType, BoundType>().name("boundType").immutable(true).many(false).mandatory(false).rawFeature(GenericExtension.Features.boundType).datatype(() -> Enums.BOUND_TYPE).defaultValue(null).build();
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).mandatory(false).rawFeature(GenericExtension.Features.parameters).concept(() -> LMCoreDefinition.Groups.GENERIC_PARAMETER).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(TYPE, BOUND_TYPE, PARAMETERS);
    }

    interface GENERIC_PARAMETER {
      Attribute<Boolean, Boolean> WILDCARD = new AttributeBuilder<Boolean, Boolean>().name("wildcard").immutable(true).many(false).mandatory(false).rawFeature(GenericParameter.Features.wildcard).datatype(() -> Units.BOOLEAN).defaultValue(null).build();
      Attribute<BoundType, BoundType> WILDCARD_BOUND_TYPE = new AttributeBuilder<BoundType, BoundType>().name("wildcardBoundType").immutable(true).many(false).mandatory(false).rawFeature(GenericParameter.Features.wildcardBoundType).datatype(() -> Enums.BOUND_TYPE).defaultValue(null).build();
      Relation<Type<?>, Type<?>> TYPE = new RelationBuilder<Type<?>, Type<?>>().name("type").immutable(true).many(false).mandatory(true).rawFeature(GenericParameter.Features.type).concept(() -> LMCoreDefinition.Groups.TYPE).lazy(true).contains(false).build();
      Relation<GenericParameter, List<GenericParameter>> PARAMETERS = new RelationBuilder<GenericParameter, List<GenericParameter>>().name("parameters").immutable(true).many(true).mandatory(false).rawFeature(GenericParameter.Features.parameters).concept(() -> LMCoreDefinition.Groups.GENERIC_PARAMETER).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(WILDCARD, WILDCARD_BOUND_TYPE, TYPE, PARAMETERS);
    }

    interface JAVA_WRAPPER {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;
      Attribute<String, String> QUALIFIED_CLASS_NAME = new AttributeBuilder<String, String>().name("qualifiedClassName").immutable(true).many(false).mandatory(true).rawFeature(JavaWrapper.Features.qualifiedClassName).datatype(() -> Units.STRING).defaultValue(null).build();
      Relation<Serializer, Serializer> SERIALIZER = new RelationBuilder<Serializer, Serializer>().name("serializer").immutable(true).many(false).mandatory(false).rawFeature(JavaWrapper.Features.serializer).concept(() -> LMCoreDefinition.Groups.SERIALIZER).lazy(false).contains(true).build();
      List<Feature<?, ?>> ALL = List.of(NAME, QUALIFIED_CLASS_NAME, SERIALIZER);
    }

    interface SERIALIZER {
      Attribute<String, String> CREATE = new AttributeBuilder<String, String>().name("create").immutable(true).many(false).mandatory(true).rawFeature(Serializer.Features.create).datatype(() -> Units.STRING).defaultValue(null).build();
      Attribute<String, String> CONVERT = new AttributeBuilder<String, String>().name("convert").immutable(true).many(false).mandatory(true).rawFeature(Serializer.Features.convert).datatype(() -> Units.STRING).defaultValue(null).build();
      List<Feature<?, ?>> ALL = List.of(CREATE, CONVERT);
    }
  }

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
      Generic<? extends LMObject> T = new GenericBuilder<LMObject>().name("T").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
      List<Generic<?>> ALL = List.of(T);
    }

    interface INCLUDE {
      Generic<? extends LMObject> T = new GenericBuilder<LMObject>().name("T").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
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
      Generic<? extends LMObject> UNARY_TYPE = new GenericBuilder<LMObject>().name("UnaryType").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
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
    Group<LMObject> LM_OBJECT = new GroupBuilder<LMObject>().name("LMObject").concrete(false).build();
    Group<Named> NAMED = new GroupBuilder<Named>().name("Named").concrete(false).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeature(() -> Features.NAMED.ALL.get(0)).build();
    Group<Type<?>> TYPE = new GroupBuilder<Type<?>>().name("Type").concrete(false).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeature(() -> Features.TYPE.ALL.get(0)).addGeneric(() -> Generics.TYPE.ALL.get(0)).build();
    Group<Model> MODEL = new GroupBuilder<Model>().name("Model").concrete(false).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeature(() -> Features.MODEL.ALL.get(0)).addFeature(() -> Features.MODEL.ALL.get(1)).addFeature(() -> Features.MODEL.ALL.get(2)).build();
    Group<MetaModel> META_MODEL = new GroupBuilder<MetaModel>().name("MetaModel").concrete(true).addInclude(() -> new IncludeBuilder<Model>().group(() -> MODEL).build()).addFeature(() -> Features.META_MODEL.ALL.get(0)).addFeature(() -> Features.META_MODEL.ALL.get(1)).addFeature(() -> Features.META_MODEL.ALL.get(2)).addFeature(() -> Features.META_MODEL.ALL.get(3)).addFeature(() -> Features.META_MODEL.ALL.get(4)).addFeature(() -> Features.META_MODEL.ALL.get(5)).addFeature(() -> Features.META_MODEL.ALL.get(6)).addFeature(() -> Features.META_MODEL.ALL.get(7)).addFeature(() -> Features.META_MODEL.ALL.get(8)).lmBuilder(new BuilderSupplier<>(MetaModelBuilder::new)).build();
    Group<Concept<?>> CONCEPT = new GroupBuilder<Concept<?>>().name("Concept").concrete(false).addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.CONCEPT.ALL.get(0)).build()).build()).addFeature(() -> Features.CONCEPT.ALL.get(0)).addGeneric(() -> Generics.CONCEPT.ALL.get(0)).build();
    Group<Group<?>> GROUP = new GroupBuilder<Group<?>>().name("Group").concrete(true).addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.GROUP.ALL.get(0)).build()).build()).addInclude(() -> new IncludeBuilder<Concept<?>>().group(() -> CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.GROUP.ALL.get(0)).build()).build()).addFeature(() -> Features.GROUP.ALL.get(0)).addFeature(() -> Features.GROUP.ALL.get(1)).addFeature(() -> Features.GROUP.ALL.get(2)).addFeature(() -> Features.GROUP.ALL.get(3)).addFeature(() -> Features.GROUP.ALL.get(4)).addFeature(() -> Features.GROUP.ALL.get(5)).addFeature(() -> Features.GROUP.ALL.get(6)).addGeneric(() -> Generics.GROUP.ALL.get(0)).lmBuilder(new BuilderSupplier<>(GroupBuilder::new)).build();
    Group<Include<?>> INCLUDE = new GroupBuilder<Include<?>>().name("Include").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeature(() -> Features.INCLUDE.ALL.get(0)).addFeature(() -> Features.INCLUDE.ALL.get(1)).addGeneric(() -> Generics.INCLUDE.ALL.get(0)).lmBuilder(new BuilderSupplier<>(IncludeBuilder::new)).build();
    Group<Feature<?, ?>> FEATURE = new GroupBuilder<Feature<?, ?>>().name("Feature").concrete(false).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeature(() -> Features.FEATURE.ALL.get(0)).addFeature(() -> Features.FEATURE.ALL.get(1)).addFeature(() -> Features.FEATURE.ALL.get(2)).addFeature(() -> Features.FEATURE.ALL.get(3)).addFeature(() -> Features.FEATURE.ALL.get(4)).addFeature(() -> Features.FEATURE.ALL.get(5)).addGeneric(() -> Generics.FEATURE.ALL.get(0)).addGeneric(() -> Generics.FEATURE.ALL.get(1)).build();
    Group<Attribute<?, ?>> ATTRIBUTE = new GroupBuilder<Attribute<?, ?>>().name("Attribute").concrete(true).addInclude(() -> new IncludeBuilder<Feature<?, ?>>().group(() -> FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.ATTRIBUTE.ALL.get(0)).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.ATTRIBUTE.ALL.get(1)).build()).build()).addFeature(() -> Features.ATTRIBUTE.ALL.get(0)).addFeature(() -> Features.ATTRIBUTE.ALL.get(1)).addFeature(() -> Features.ATTRIBUTE.ALL.get(2)).addFeature(() -> Features.ATTRIBUTE.ALL.get(3)).addFeature(() -> Features.ATTRIBUTE.ALL.get(4)).addFeature(() -> Features.ATTRIBUTE.ALL.get(5)).addFeature(() -> Features.ATTRIBUTE.ALL.get(6)).addFeature(() -> Features.ATTRIBUTE.ALL.get(7)).addGeneric(() -> Generics.ATTRIBUTE.ALL.get(0)).addGeneric(() -> Generics.ATTRIBUTE.ALL.get(1)).lmBuilder(new BuilderSupplier<>(AttributeBuilder::new)).build();
    Group<Relation<?, ?>> RELATION = new GroupBuilder<Relation<?, ?>>().name("Relation").concrete(true).addInclude(() -> new IncludeBuilder<Feature<?, ?>>().group(() -> FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.RELATION.ALL.get(0)).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.RELATION.ALL.get(1)).build()).build()).addFeature(() -> Features.RELATION.ALL.get(0)).addFeature(() -> Features.RELATION.ALL.get(1)).addFeature(() -> Features.RELATION.ALL.get(2)).addFeature(() -> Features.RELATION.ALL.get(3)).addFeature(() -> Features.RELATION.ALL.get(4)).addFeature(() -> Features.RELATION.ALL.get(5)).addFeature(() -> Features.RELATION.ALL.get(6)).addFeature(() -> Features.RELATION.ALL.get(7)).addFeature(() -> Features.RELATION.ALL.get(8)).addGeneric(() -> Generics.RELATION.ALL.get(0)).addGeneric(() -> Generics.RELATION.ALL.get(1)).lmBuilder(new BuilderSupplier<>(RelationBuilder::new)).build();
    Group<Operation> OPERATION = new GroupBuilder<Operation>().name("Operation").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeature(() -> Features.OPERATION.ALL.get(0)).addFeature(() -> Features.OPERATION.ALL.get(1)).addFeature(() -> Features.OPERATION.ALL.get(2)).addFeature(() -> Features.OPERATION.ALL.get(3)).addFeature(() -> Features.OPERATION.ALL.get(4)).lmBuilder(new BuilderSupplier<>(OperationBuilder::new)).build();
    Group<OperationParameter> OPERATION_PARAMETER = new GroupBuilder<OperationParameter>().name("OperationParameter").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeature(() -> Features.OPERATION_PARAMETER.ALL.get(0)).addFeature(() -> Features.OPERATION_PARAMETER.ALL.get(1)).addFeature(() -> Features.OPERATION_PARAMETER.ALL.get(2)).lmBuilder(new BuilderSupplier<>(OperationParameterBuilder::new)).build();
    Group<Datatype<?>> DATATYPE = new GroupBuilder<Datatype<?>>().name("Datatype").concrete(false).addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.DATATYPE.ALL.get(0)).build()).build()).addFeature(() -> Features.DATATYPE.ALL.get(0)).addGeneric(() -> Generics.DATATYPE.ALL.get(0)).build();
    Group<Alias> ALIAS = new GroupBuilder<Alias>().name("Alias").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeature(() -> Features.ALIAS.ALL.get(0)).addFeature(() -> Features.ALIAS.ALL.get(1)).lmBuilder(new BuilderSupplier<>(AliasBuilder::new)).build();
    Group<Enum<?>> ENUM = new GroupBuilder<Enum<?>>().name("Enum").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.ENUM.ALL.get(0)).build()).build()).addFeature(() -> Features.ENUM.ALL.get(0)).addFeature(() -> Features.ENUM.ALL.get(1)).addGeneric(() -> Generics.ENUM.ALL.get(0)).lmBuilder(new BuilderSupplier<>(EnumBuilder::new)).build();
    Group<Unit<?>> UNIT = new GroupBuilder<Unit<?>>().name("Unit").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.UNIT.ALL.get(0)).build()).build()).addFeature(() -> Features.UNIT.ALL.get(0)).addFeature(() -> Features.UNIT.ALL.get(1)).addFeature(() -> Features.UNIT.ALL.get(2)).addFeature(() -> Features.UNIT.ALL.get(3)).addFeature(() -> Features.UNIT.ALL.get(4)).addGeneric(() -> Generics.UNIT.ALL.get(0)).lmBuilder(new BuilderSupplier<>(UnitBuilder::new)).build();
    Group<Generic<?>> GENERIC = new GroupBuilder<Generic<?>>().name("Generic").concrete(true).addInclude(() -> new IncludeBuilder<Concept<?>>().group(() -> CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.GENERIC.ALL.get(0)).build()).build()).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.GENERIC.ALL.get(0)).build()).build()).addFeature(() -> Features.GENERIC.ALL.get(0)).addFeature(() -> Features.GENERIC.ALL.get(1)).addGeneric(() -> Generics.GENERIC.ALL.get(0)).lmBuilder(new BuilderSupplier<>(GenericBuilder::new)).build();
    Group<GenericExtension> GENERIC_EXTENSION = new GroupBuilder<GenericExtension>().name("GenericExtension").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeature(() -> Features.GENERIC_EXTENSION.ALL.get(0)).addFeature(() -> Features.GENERIC_EXTENSION.ALL.get(1)).addFeature(() -> Features.GENERIC_EXTENSION.ALL.get(2)).lmBuilder(new BuilderSupplier<>(GenericExtensionBuilder::new)).build();
    Group<GenericParameter> GENERIC_PARAMETER = new GroupBuilder<GenericParameter>().name("GenericParameter").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeature(() -> Features.GENERIC_PARAMETER.ALL.get(0)).addFeature(() -> Features.GENERIC_PARAMETER.ALL.get(1)).addFeature(() -> Features.GENERIC_PARAMETER.ALL.get(2)).addFeature(() -> Features.GENERIC_PARAMETER.ALL.get(3)).lmBuilder(new BuilderSupplier<>(GenericParameterBuilder::new)).build();
    Group<JavaWrapper<?>> JAVA_WRAPPER = new GroupBuilder<JavaWrapper<?>>().name("JavaWrapper").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreDefinition.Generics.JAVA_WRAPPER.ALL.get(0)).build()).build()).addFeature(() -> Features.JAVA_WRAPPER.ALL.get(0)).addFeature(() -> Features.JAVA_WRAPPER.ALL.get(1)).addFeature(() -> Features.JAVA_WRAPPER.ALL.get(2)).addGeneric(() -> Generics.JAVA_WRAPPER.ALL.get(0)).lmBuilder(new BuilderSupplier<>(JavaWrapperBuilder::new)).build();
    Group<Serializer> SERIALIZER = new GroupBuilder<Serializer>().name("Serializer").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeature(() -> Features.SERIALIZER.ALL.get(0)).addFeature(() -> Features.SERIALIZER.ALL.get(1)).lmBuilder(new BuilderSupplier<>(SerializerBuilder::new)).build();
    List<Group<?>> ALL = List.of(LM_OBJECT, NAMED, TYPE, MODEL, META_MODEL, CONCEPT, GROUP, INCLUDE, FEATURE, ATTRIBUTE, RELATION, OPERATION, OPERATION_PARAMETER, DATATYPE, ALIAS, ENUM, UNIT, GENERIC, GENERIC_EXTENSION, GENERIC_PARAMETER, JAVA_WRAPPER, SERIALIZER);
  }

  interface Units {
    Unit<String> MATCHER = new UnitBuilder<String>().name("matcher").matcher("rgx_match:<(.+?)>").defaultValue(null).primitive(Primitive.String).extractor(null).build();
    Unit<String> EXTRACTOR = new UnitBuilder<String>().name("extractor").matcher("rgx_match:<(.+?)>").defaultValue(null).primitive(Primitive.String).extractor(null).build();
    Unit<Boolean> BOOLEAN = new UnitBuilder<Boolean>().name("boolean").matcher("rgx_match:<(true|false)>").defaultValue("false").primitive(Primitive.Boolean).extractor(null).build();
    Unit<Integer> INT = new UnitBuilder<Integer>().name("int").matcher("rgx_match:<[0-9]+>").defaultValue("0").primitive(Primitive.Int).extractor(null).build();
    Unit<Long> LONG = new UnitBuilder<Long>().name("long").matcher("rgx_match:<[0-9]+[Ll]>").defaultValue("0L").primitive(Primitive.Long).extractor(null).build();
    Unit<Float> FLOAT = new UnitBuilder<Float>().name("float").matcher("rgx_match:<[0-9.]+[Ff]>").defaultValue("0f").primitive(Primitive.Float).extractor(null).build();
    Unit<Double> DOUBLE = new UnitBuilder<Double>().name("double").matcher("rgx_match:<[0-9.]+>").defaultValue("0.").primitive(Primitive.Double).extractor(null).build();
    Unit<String> STRING = new UnitBuilder<String>().name("string").matcher(null).defaultValue(null).primitive(Primitive.String).extractor(null).build();
    List<Unit<?>> ALL = List.of(MATCHER, EXTRACTOR, BOOLEAN, INT, LONG, FLOAT, DOUBLE, STRING);
  }

  interface Enums {
    Enum<BoundType> BOUND_TYPE = new EnumBuilder<BoundType>().name("BoundType").addLiteral("extends").addLiteral("super").build();
    Enum<Primitive> PRIMITIVE = new EnumBuilder<Primitive>().name("Primitive").addLiteral("boolean").addLiteral("int").addLiteral("long").addLiteral("float").addLiteral("double").addLiteral("string").build();
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
    JavaWrapper<RawFeature<?, ?>> RAW_FEATURE = new JavaWrapperBuilder<RawFeature<?, ?>>().name("RawFeature").qualifiedClassName("org.logoce.lmf.model.api.feature.RawFeature").serializer(() -> null).build();
    JavaWrapper<IModelPackage> I_MODEL_PACKAGE = new JavaWrapperBuilder<IModelPackage>().name("IModelPackage").qualifiedClassName("org.logoce.lmf.model.api.model.IModelPackage").serializer(() -> null).build();
    JavaWrapper<BuilderSupplier<?>> BUILDER_SUPPLIER = new JavaWrapperBuilder<BuilderSupplier<?>>().name("BuilderSupplier").qualifiedClassName("org.logoce.lmf.model.api.model.BuilderSupplier").serializer(() -> null).build();
    List<JavaWrapper<?>> ALL = List.of(RAW_FEATURE, I_MODEL_PACKAGE, BUILDER_SUPPLIER);
  }
}
