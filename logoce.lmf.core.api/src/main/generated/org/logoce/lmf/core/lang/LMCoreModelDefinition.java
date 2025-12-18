package org.logoce.lmf.core.lang;

import java.util.List;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.api.model.IModelPackage;
import org.logoce.lmf.core.lang.builder.AliasBuilder;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.EnumAttributeBuilder;
import org.logoce.lmf.core.lang.builder.EnumBuilder;
import org.logoce.lmf.core.lang.builder.GenericBuilder;
import org.logoce.lmf.core.lang.builder.GenericExtensionBuilder;
import org.logoce.lmf.core.lang.builder.GenericParameterBuilder;
import org.logoce.lmf.core.lang.builder.GroupBuilder;
import org.logoce.lmf.core.lang.builder.IncludeBuilder;
import org.logoce.lmf.core.lang.builder.JavaWrapperBuilder;
import org.logoce.lmf.core.lang.builder.MetaModelBuilder;
import org.logoce.lmf.core.lang.builder.OperationBuilder;
import org.logoce.lmf.core.lang.builder.OperationParameterBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;
import org.logoce.lmf.core.lang.builder.SerializerBuilder;
import org.logoce.lmf.core.lang.builder.UnitBuilder;

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
      Generic<?> LISTENER_TYPE = new GenericBuilder<>().name("ListenerType").build();
      Generic<?> PARENT_GROUP = new GenericBuilder<>().name("ParentGroup").build();
      List<Generic<?>> ALL = List.of(UNARY_TYPE, EFFECTIVE_TYPE, LISTENER_TYPE, PARENT_GROUP);
    }

    interface ATTRIBUTE {
      Generic<?> UNARY_TYPE = new GenericBuilder<>().name("UnaryType").build();
      Generic<?> EFFECTIVE_TYPE = new GenericBuilder<>().name("EffectiveType").build();
      Generic<?> LISTENER_TYPE = new GenericBuilder<>().name("ListenerType").build();
      Generic<?> PARENT_GROUP = new GenericBuilder<>().name("ParentGroup").build();
      List<Generic<?>> ALL = List.of(UNARY_TYPE, EFFECTIVE_TYPE, LISTENER_TYPE, PARENT_GROUP);
    }

    interface RELATION {
      Generic<? extends LMObject> UNARY_TYPE = new GenericBuilder<LMObject>().name("UnaryType").extension(() -> new GenericExtensionBuilder().type(() -> LMCoreModelDefinition.Groups.LM_OBJECT).boundType(BoundType.Extends).build()).build();
      Generic<?> EFFECTIVE_TYPE = new GenericBuilder<>().name("EffectiveType").build();
      Generic<?> LISTENER_TYPE = new GenericBuilder<>().name("ListenerType").build();
      Generic<?> PARENT_GROUP = new GenericBuilder<>().name("ParentGroup").build();
      List<Generic<?>> ALL = List.of(UNARY_TYPE, EFFECTIVE_TYPE, LISTENER_TYPE, PARENT_GROUP);
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
    Group<LMObject> LM_OBJECT = new GroupBuilder<LMObject>().name("LMObject").addFeatures(LMObject.Features.ALL).build();
    Group<Named> NAMED = new GroupBuilder<Named>().name("Named").addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Named.Features.ALL).build();
    Group<Type<?>> TYPE = new GroupBuilder<Type<?>>().name("Type").addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Type.Features.ALL).addGenerics(Generics.TYPE.ALL).build();
    Group<Model> MODEL = new GroupBuilder<Model>().name("Model").addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Model.Features.ALL).build();
    Group<MetaModel> META_MODEL = new GroupBuilder<MetaModel>().name("MetaModel").concrete(true).addInclude(() -> new IncludeBuilder<Model>().group(() -> MODEL).build()).addFeatures(MetaModel.Features.ALL).lmBuilder(new BuilderSupplier<>(MetaModelBuilder::new)).build();
    Group<Concept<?>> CONCEPT = new GroupBuilder<Concept<?>>().name("Concept").addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.CONCEPT.T).build()).build()).addFeatures(Concept.Features.ALL).addGenerics(Generics.CONCEPT.ALL).build();
    Group<Group<?>> GROUP = new GroupBuilder<Group<?>>().name("Group").concrete(true).addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.T).build()).build()).addInclude(() -> new IncludeBuilder<Concept<?>>().group(() -> CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GROUP.T).build()).build()).addFeatures(Group.Features.ALL).addGenerics(Generics.GROUP.ALL).lmBuilder(new BuilderSupplier<>(GroupBuilder::new)).build();
    Group<Include<?>> INCLUDE = new GroupBuilder<Include<?>>().name("Include").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Include.Features.ALL).addGenerics(Generics.INCLUDE.ALL).lmBuilder(new BuilderSupplier<>(IncludeBuilder::new)).build();
    Group<Feature<?, ?, ?, ?>> FEATURE = new GroupBuilder<Feature<?, ?, ?, ?>>().name("Feature").addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Feature.Features.ALL).addGenerics(Generics.FEATURE.ALL).build();
    Group<Attribute<?, ?, ?, ?>> ATTRIBUTE = new GroupBuilder<Attribute<?, ?, ?, ?>>().name("Attribute").concrete(true).addInclude(() -> new IncludeBuilder<Feature<?, ?, ?, ?>>().group(() -> FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.UNARY_TYPE).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.EFFECTIVE_TYPE).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.LISTENER_TYPE).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ATTRIBUTE.PARENT_GROUP).build()).build()).addFeatures(Attribute.Features.ALL).addGenerics(Generics.ATTRIBUTE.ALL).lmBuilder(new BuilderSupplier<>(AttributeBuilder::new)).build();
    Group<Relation<?, ?, ?, ?>> RELATION = new GroupBuilder<Relation<?, ?, ?, ?>>().name("Relation").concrete(true).addInclude(() -> new IncludeBuilder<Feature<?, ?, ?, ?>>().group(() -> FEATURE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.UNARY_TYPE).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.EFFECTIVE_TYPE).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.LISTENER_TYPE).build()).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.RELATION.PARENT_GROUP).build()).build()).addFeatures(Relation.Features.ALL).addGenerics(Generics.RELATION.ALL).lmBuilder(new BuilderSupplier<>(RelationBuilder::new)).build();
    Group<Operation> OPERATION = new GroupBuilder<Operation>().name("Operation").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Operation.Features.ALL).lmBuilder(new BuilderSupplier<>(OperationBuilder::new)).build();
    Group<OperationParameter> OPERATION_PARAMETER = new GroupBuilder<OperationParameter>().name("OperationParameter").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(OperationParameter.Features.ALL).lmBuilder(new BuilderSupplier<>(OperationParameterBuilder::new)).build();
    Group<Datatype<?>> DATATYPE = new GroupBuilder<Datatype<?>>().name("Datatype").addInclude(() -> new IncludeBuilder<Type<?>>().group(() -> TYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.DATATYPE.T).build()).build()).addFeatures(Datatype.Features.ALL).addGenerics(Generics.DATATYPE.ALL).build();
    Group<Alias> ALIAS = new GroupBuilder<Alias>().name("Alias").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(Alias.Features.ALL).lmBuilder(new BuilderSupplier<>(AliasBuilder::new)).build();
    Group<Enum<?>> ENUM = new GroupBuilder<Enum<?>>().name("Enum").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.ENUM.T).build()).build()).addFeatures(Enum.Features.ALL).addGenerics(Generics.ENUM.ALL).lmBuilder(new BuilderSupplier<>(EnumBuilder::new)).build();
    Group<EnumAttribute> ENUM_ATTRIBUTE = new GroupBuilder<EnumAttribute>().name("EnumAttribute").concrete(true).addInclude(() -> new IncludeBuilder<Named>().group(() -> NAMED).build()).addFeatures(EnumAttribute.Features.ALL).lmBuilder(new BuilderSupplier<>(EnumAttributeBuilder::new)).build();
    Group<Unit<?>> UNIT = new GroupBuilder<Unit<?>>().name("Unit").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.UNIT.T).build()).build()).addFeatures(Unit.Features.ALL).addGenerics(Generics.UNIT.ALL).lmBuilder(new BuilderSupplier<>(UnitBuilder::new)).build();
    Group<Generic<?>> GENERIC = new GroupBuilder<Generic<?>>().name("Generic").concrete(true).addInclude(() -> new IncludeBuilder<Concept<?>>().group(() -> CONCEPT).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GENERIC.T).build()).build()).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.GENERIC.T).build()).build()).addFeatures(Generic.Features.ALL).addGenerics(Generics.GENERIC.ALL).lmBuilder(new BuilderSupplier<>(GenericBuilder::new)).build();
    Group<GenericExtension> GENERIC_EXTENSION = new GroupBuilder<GenericExtension>().name("GenericExtension").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(GenericExtension.Features.ALL).lmBuilder(new BuilderSupplier<>(GenericExtensionBuilder::new)).build();
    Group<GenericParameter> GENERIC_PARAMETER = new GroupBuilder<GenericParameter>().name("GenericParameter").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(GenericParameter.Features.ALL).lmBuilder(new BuilderSupplier<>(GenericParameterBuilder::new)).build();
    Group<JavaWrapper<?>> JAVA_WRAPPER = new GroupBuilder<JavaWrapper<?>>().name("JavaWrapper").concrete(true).addInclude(() -> new IncludeBuilder<Datatype<?>>().group(() -> DATATYPE).addParameter(() -> new GenericParameterBuilder().type(() -> LMCoreModelDefinition.Generics.JAVA_WRAPPER.T).build()).build()).addFeatures(JavaWrapper.Features.ALL).addGenerics(Generics.JAVA_WRAPPER.ALL).lmBuilder(new BuilderSupplier<>(JavaWrapperBuilder::new)).build();
    Group<Serializer> SERIALIZER = new GroupBuilder<Serializer>().name("Serializer").concrete(true).addInclude(() -> new IncludeBuilder<LMObject>().group(() -> LM_OBJECT).build()).addFeatures(Serializer.Features.ALL).lmBuilder(new BuilderSupplier<>(SerializerBuilder::new)).build();
    List<Group<?>> ALL = List.of(LM_OBJECT, NAMED, TYPE, MODEL, META_MODEL, CONCEPT, GROUP, INCLUDE, FEATURE, ATTRIBUTE, RELATION, OPERATION, OPERATION_PARAMETER, DATATYPE, ALIAS, ENUM, ENUM_ATTRIBUTE, UNIT, GENERIC, GENERIC_EXTENSION, GENERIC_PARAMETER, JAVA_WRAPPER, SERIALIZER);
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
    JavaWrapper<IModelPackage> I_MODEL_PACKAGE = new JavaWrapperBuilder<IModelPackage>().name("IModelPackage").qualifiedClassName("org.logoce.lmf.core.api.model.IModelPackage").build();
    JavaWrapper<BuilderSupplier<?>> BUILDER_SUPPLIER = new JavaWrapperBuilder<BuilderSupplier<?>>().name("BuilderSupplier").qualifiedClassName("org.logoce.lmf.core.api.model.BuilderSupplier").build();
    List<JavaWrapper<?>> ALL = List.of(I_MODEL_PACKAGE, BUILDER_SUPPLIER);
  }
}
