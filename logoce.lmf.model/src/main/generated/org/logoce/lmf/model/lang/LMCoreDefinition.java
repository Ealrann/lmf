package org.logoce.lmf.model.lang;

import java.lang.Boolean;
import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.impl.AliasImpl;
import org.logoce.lmf.model.lang.impl.AttributeImpl;
import org.logoce.lmf.model.lang.impl.EnumImpl;
import org.logoce.lmf.model.lang.impl.GenericImpl;
import org.logoce.lmf.model.lang.impl.GroupImpl;
import org.logoce.lmf.model.lang.impl.JavaWrapperImpl;
import org.logoce.lmf.model.lang.impl.ReferenceImpl;
import org.logoce.lmf.model.lang.impl.RelationImpl;
import org.logoce.lmf.model.lang.impl.UnitImpl;

public interface LMCoreDefinition {
  interface Features {
    interface LM_OBJECT {
      List<Feature<?, ?>> ALL = List.of();
    }

    interface NAMED {
      Attribute<String, String> NAME = new AttributeImpl<>("name", true, false, true, Named.Features.name, Units.STRING, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface TYPE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface MODEL {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<String, String> DOMAIN = new AttributeImpl<>("domain", true, false, true, Model.Features.domain, Units.STRING, null, List.of());

      Attribute<String, List<String>> IMPORTS = new AttributeImpl<>("imports", true, true, false, Model.Features.imports, Units.STRING, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS);
    }

    interface META_MODEL {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<String, String> DOMAIN = LMCoreDefinition.Features.MODEL.DOMAIN;

      Attribute<String, List<String>> IMPORTS = LMCoreDefinition.Features.MODEL.IMPORTS;

      Relation<Group<?>, List<Group<?>>> GROUPS = new RelationImpl<>("groups", true, true, false, MetaModel.Features.groups, new ReferenceImpl<>(() -> Groups.GROUP, List.of()), false, true);

      Relation<Enum<?>, List<Enum<?>>> ENUMS = new RelationImpl<>("enums", true, true, false, MetaModel.Features.enums, new ReferenceImpl<>(() -> Groups.ENUM, List.of()), false, true);

      Relation<Unit<?>, List<Unit<?>>> UNITS = new RelationImpl<>("units", true, true, false, MetaModel.Features.units, new ReferenceImpl<>(() -> Groups.UNIT, List.of()), false, true);

      Relation<Alias, List<Alias>> ALIASES = new RelationImpl<>("aliases", true, true, false, MetaModel.Features.aliases, new ReferenceImpl<>(() -> Groups.ALIAS, List.of()), false, true);

      Relation<JavaWrapper<?>, List<JavaWrapper<?>>> JAVA_WRAPPERS = new RelationImpl<>("javaWrappers", true, true, false, MetaModel.Features.javaWrappers, new ReferenceImpl<>(() -> Groups.JAVA_WRAPPER, List.of()), false, true);

      Attribute<IModelPackage, IModelPackage> L_PACKAGE = new AttributeImpl<>("lPackage", true, false, true, MetaModel.Features.lPackage, JavaWrappers.I_MODEL_PACKAGE, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN, IMPORTS, GROUPS, ENUMS, UNITS, ALIASES, JAVA_WRAPPERS, L_PACKAGE);
    }

    interface CONCEPT {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface GROUP {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<Boolean, Boolean> CONCRETE = new AttributeImpl<>("concrete", true, false, false, Group.Features.concrete, Units.BOOLEAN, null, List.of());

      Relation<Reference<?>, List<Reference<?>>> INCLUDES = new RelationImpl<>("includes", true, true, false, Group.Features.includes, new ReferenceImpl<>(() -> Groups.REFERENCE, List.of()), false, true);

      Relation<Feature<?, ?>, List<Feature<?, ?>>> FEATURES = new RelationImpl<>("features", true, true, false, Group.Features.features, new ReferenceImpl<>(() -> Groups.FEATURE, List.of()), false, true);

      Relation<Generic<?>, List<Generic<?>>> GENERICS = new RelationImpl<>("generics", true, true, false, Group.Features.generics, new ReferenceImpl<>(() -> Groups.GENERIC, List.of()), false, true);

      List<Feature<?, ?>> ALL = List.of(NAME, CONCRETE, INCLUDES, FEATURES, GENERICS);
    }

    interface FEATURE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<Boolean, Boolean> IMMUTABLE = new AttributeImpl<>("immutable", true, false, false, Feature.Features.immutable, Units.BOOLEAN, null, List.of());

      Attribute<Boolean, Boolean> MANY = new AttributeImpl<>("many", true, false, false, Feature.Features.many, Units.BOOLEAN, null, List.of());

      Attribute<Boolean, Boolean> MANDATORY = new AttributeImpl<>("mandatory", true, false, false, Feature.Features.mandatory, Units.BOOLEAN, null, List.of());

      Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = new AttributeImpl<>("rawFeature", true, false, false, Feature.Features.rawFeature, JavaWrappers.RAW_FEATURE, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, RAW_FEATURE);
    }

    interface ATTRIBUTE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<Boolean, Boolean> IMMUTABLE = LMCoreDefinition.Features.FEATURE.IMMUTABLE;

      Attribute<Boolean, Boolean> MANY = LMCoreDefinition.Features.FEATURE.MANY;

      Attribute<Boolean, Boolean> MANDATORY = LMCoreDefinition.Features.FEATURE.MANDATORY;

      Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = LMCoreDefinition.Features.FEATURE.RAW_FEATURE;

      Relation<Datatype<?>, Datatype<?>> DATATYPE = new RelationImpl<>("datatype", true, false, true, Attribute.Features.datatype, new ReferenceImpl<>(() -> Groups.DATATYPE, List.of(() -> LMCoreDefinition.Generics.ATTRIBUTE.get(0))), false, false);

      Attribute<String, String> DEFAULT_VALUE = new AttributeImpl<>("defaultValue", true, false, false, Attribute.Features.defaultValue, Units.STRING, null, List.of());

      Relation<Generic<?>, List<Generic<?>>> PARAMETERS = new RelationImpl<>("parameters", true, true, false, Attribute.Features.parameters, new ReferenceImpl<>(() -> Groups.GENERIC, List.of()), false, false);

      List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, RAW_FEATURE, DATATYPE, DEFAULT_VALUE, PARAMETERS);
    }

    interface RELATION {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<Boolean, Boolean> IMMUTABLE = LMCoreDefinition.Features.FEATURE.IMMUTABLE;

      Attribute<Boolean, Boolean> MANY = LMCoreDefinition.Features.FEATURE.MANY;

      Attribute<Boolean, Boolean> MANDATORY = LMCoreDefinition.Features.FEATURE.MANDATORY;

      Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = LMCoreDefinition.Features.FEATURE.RAW_FEATURE;

      Relation<Reference<?>, Reference<?>> REFERENCE = new RelationImpl<>("reference", true, false, true, Relation.Features.reference, new ReferenceImpl<>(() -> Groups.REFERENCE, List.of(() -> LMCoreDefinition.Generics.RELATION.get(0))), false, true);

      Attribute<Boolean, Boolean> LAZY = new AttributeImpl<>("lazy", true, false, false, Relation.Features.lazy, Units.BOOLEAN, null, List.of());

      Attribute<Boolean, Boolean> CONTAINS = new AttributeImpl<>("contains", true, false, false, Relation.Features.contains, Units.BOOLEAN, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, RAW_FEATURE, REFERENCE, LAZY, CONTAINS);
    }

    interface DATATYPE {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      List<Feature<?, ?>> ALL = List.of(NAME);
    }

    interface ALIAS {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<String, String> VALUE = new AttributeImpl<>("value", true, false, true, Alias.Features.value, Units.STRING, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, VALUE);
    }

    interface ENUM {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<String, List<String>> LITERALS = new AttributeImpl<>("literals", true, true, false, Enum.Features.literals, Units.STRING, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, LITERALS);
    }

    interface UNIT {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<String, String> MATCHER = new AttributeImpl<>("matcher", true, false, false, Unit.Features.matcher, Units.MATCHER, null, List.of());

      Attribute<String, String> DEFAULT_VALUE = new AttributeImpl<>("defaultValue", true, false, false, Unit.Features.defaultValue, Units.STRING, null, List.of());

      Attribute<Primitive, Primitive> PRIMITIVE = new AttributeImpl<>("primitive", true, false, false, Unit.Features.primitive, Enums.PRIMITIVE, "String", List.of());

      Attribute<String, String> EXTRACTOR = new AttributeImpl<>("extractor", true, false, false, Unit.Features.extractor, Units.EXTRACTOR, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, MATCHER, DEFAULT_VALUE, PRIMITIVE, EXTRACTOR);
    }

    interface GENERIC {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Relation<Type<?>, Type<?>> TYPE = new RelationImpl<>("type", true, false, false, Generic.Features.type, new ReferenceImpl<>(() -> Groups.TYPE, List.of(() -> LMCoreDefinition.Generics.GENERIC.get(0))), false, false);

      Attribute<BoundType, BoundType> BOUND_TYPE = new AttributeImpl<>("boundType", true, false, false, Generic.Features.boundType, Enums.BOUND_TYPE, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, TYPE, BOUND_TYPE);
    }

    interface REFERENCE {
      Relation<Concept<?>, Concept<?>> GROUP = new RelationImpl<>("group", true, false, true, Reference.Features.group, new ReferenceImpl<>(() -> Groups.CONCEPT, List.of(() -> LMCoreDefinition.Generics.REFERENCE.get(0))), true, false);

      Relation<Concept<?>, List<Concept<?>>> PARAMETERS = new RelationImpl<>("parameters", true, true, false, Reference.Features.parameters, new ReferenceImpl<>(() -> Groups.CONCEPT, List.of()), true, false);

      List<Feature<?, ?>> ALL = List.of(GROUP, PARAMETERS);
    }

    interface JAVA_WRAPPER {
      Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

      Attribute<String, String> DOMAIN = new AttributeImpl<>("domain", true, false, true, JavaWrapper.Features.domain, Units.STRING, null, List.of());

      List<Feature<?, ?>> ALL = List.of(NAME, DOMAIN);
    }
  }

  interface Generics {
    List<Generic<?>> TYPE = List.of(new GenericImpl<>("T", null, null));

    List<Generic<?>> CONCEPT = List.of(new GenericImpl<>("T", null, null));

    List<Generic<?>> GROUP = List.of(new GenericImpl<>("T", LMCoreDefinition.Groups.LM_OBJECT, BoundType.Extends));

    List<Generic<?>> FEATURE = List.of(new GenericImpl<>("UnaryType", null, null),new GenericImpl<>("EffectiveType", null, null));

    List<Generic<?>> ATTRIBUTE = List.of(new GenericImpl<>("UnaryType", null, null),new GenericImpl<>("EffectiveType", null, null));

    List<Generic<?>> RELATION = List.of(new GenericImpl<>("UnaryType", LMCoreDefinition.Groups.LM_OBJECT, BoundType.Extends),new GenericImpl<>("EffectiveType", null, null));

    List<Generic<?>> DATATYPE = List.of(new GenericImpl<>("T", null, null));

    List<Generic<?>> ENUM = List.of(new GenericImpl<>("T", null, null));

    List<Generic<?>> UNIT = List.of(new GenericImpl<>("T", null, null));

    List<Generic<?>> GENERIC = List.of(new GenericImpl<>("T", LMCoreDefinition.Groups.LM_OBJECT, BoundType.Extends));

    List<Generic<?>> REFERENCE = List.of(new GenericImpl<>("T", LMCoreDefinition.Groups.LM_OBJECT, BoundType.Extends));

    List<Generic<?>> JAVA_WRAPPER = List.of(new GenericImpl<>("T", null, null));
  }

  interface Groups {
    Group<LMObject> LM_OBJECT = new GroupImpl<>("LMObject", false, List.of(), Features.LM_OBJECT.ALL,List.of());

    Group<Named> NAMED = new GroupImpl<>("Named", false, List.of(new ReferenceImpl<>(() -> LM_OBJECT, List.of())), Features.NAMED.ALL,List.of());

    Group<Type<?>> TYPE = new GroupImpl<>("Type", false, List.of(new ReferenceImpl<>(() -> NAMED, List.of())), Features.TYPE.ALL,Generics.TYPE);

    Group<Model> MODEL = new GroupImpl<>("Model", false, List.of(new ReferenceImpl<>(() -> NAMED, List.of())), Features.MODEL.ALL,List.of());

    Group<MetaModel> META_MODEL = new GroupImpl<>("MetaModel", true, List.of(new ReferenceImpl<>(() -> MODEL, List.of())), Features.META_MODEL.ALL,List.of());

    Group<Concept<?>> CONCEPT = new GroupImpl<>("Concept", false, List.of(new ReferenceImpl<>(() -> NAMED, List.of())), Features.CONCEPT.ALL,Generics.CONCEPT);

    Group<Group<?>> GROUP = new GroupImpl<>("Group", true, List.of(new ReferenceImpl<>(() -> TYPE, List.of(() -> LMCoreDefinition.Generics.GROUP.get(0))),new ReferenceImpl<>(() -> CONCEPT, List.of(() -> LMCoreDefinition.Generics.GROUP.get(0)))), Features.GROUP.ALL,Generics.GROUP);

    Group<Feature<?, ?>> FEATURE = new GroupImpl<>("Feature", false, List.of(new ReferenceImpl<>(() -> NAMED, List.of())), Features.FEATURE.ALL,Generics.FEATURE);

    Group<Attribute<?, ?>> ATTRIBUTE = new GroupImpl<>("Attribute", true, List.of(new ReferenceImpl<>(() -> FEATURE, List.of(() -> LMCoreDefinition.Generics.ATTRIBUTE.get(0),() -> LMCoreDefinition.Generics.ATTRIBUTE.get(1)))), Features.ATTRIBUTE.ALL,Generics.ATTRIBUTE);

    Group<Relation<?, ?>> RELATION = new GroupImpl<>("Relation", true, List.of(new ReferenceImpl<>(() -> FEATURE, List.of(() -> LMCoreDefinition.Generics.RELATION.get(0),() -> LMCoreDefinition.Generics.RELATION.get(1)))), Features.RELATION.ALL,Generics.RELATION);

    Group<Datatype<?>> DATATYPE = new GroupImpl<>("Datatype", false, List.of(new ReferenceImpl<>(() -> TYPE, List.of(() -> LMCoreDefinition.Generics.DATATYPE.get(0)))), Features.DATATYPE.ALL,Generics.DATATYPE);

    Group<Alias> ALIAS = new GroupImpl<>("Alias", true, List.of(new ReferenceImpl<>(() -> NAMED, List.of())), Features.ALIAS.ALL,List.of());

    Group<Enum<?>> ENUM = new GroupImpl<>("Enum", true, List.of(new ReferenceImpl<>(() -> DATATYPE, List.of(() -> LMCoreDefinition.Generics.ENUM.get(0)))), Features.ENUM.ALL,Generics.ENUM);

    Group<Unit<?>> UNIT = new GroupImpl<>("Unit", true, List.of(new ReferenceImpl<>(() -> DATATYPE, List.of(() -> LMCoreDefinition.Generics.UNIT.get(0)))), Features.UNIT.ALL,Generics.UNIT);

    Group<Generic<?>> GENERIC = new GroupImpl<>("Generic", true, List.of(new ReferenceImpl<>(() -> CONCEPT, List.of(() -> LMCoreDefinition.Generics.GENERIC.get(0)))), Features.GENERIC.ALL,Generics.GENERIC);

    Group<Reference<?>> REFERENCE = new GroupImpl<>("Reference", true, List.of(new ReferenceImpl<>(() -> LM_OBJECT, List.of())), Features.REFERENCE.ALL,Generics.REFERENCE);

    Group<JavaWrapper<?>> JAVA_WRAPPER = new GroupImpl<>("JavaWrapper", true, List.of(new ReferenceImpl<>(() -> DATATYPE, List.of(() -> LMCoreDefinition.Generics.JAVA_WRAPPER.get(0)))), Features.JAVA_WRAPPER.ALL,Generics.JAVA_WRAPPER);

    List<Group<?>> ALL = List.of(LM_OBJECT, NAMED, TYPE, MODEL, META_MODEL, CONCEPT, GROUP, FEATURE, ATTRIBUTE, RELATION, DATATYPE, ALIAS, ENUM, UNIT, GENERIC, REFERENCE, JAVA_WRAPPER);
  }

  interface Units {
    Unit<String> MATCHER = new UnitImpl<>("matcher", "rgx_match:<(.+?)>", null, Primitive.String, null);

    Unit<String> EXTRACTOR = new UnitImpl<>("extractor", "rgx_match:<(.+?)>", null, Primitive.String, null);

    Unit<Boolean> BOOLEAN = new UnitImpl<>("boolean", "rgx_match:<(true|false)>", "false", Primitive.Boolean, null);

    Unit<Integer> INT = new UnitImpl<>("int", "rgx_match:<[0-9]+>", "0", Primitive.Int, null);

    Unit<Long> LONG = new UnitImpl<>("long", "rgx_match:<[0-9]+[Ll]>", "0L", Primitive.Long, null);

    Unit<Float> FLOAT = new UnitImpl<>("float", "rgx_match:<[0-9.]+[Ff]>", "0f", Primitive.Float, null);

    Unit<Double> DOUBLE = new UnitImpl<>("double", "rgx_match:<[0-9.]+>", "0.", Primitive.Double, null);

    Unit<String> STRING = new UnitImpl<>("string", null, null, Primitive.String, null);

    List<Unit<?>> ALL = List.of(MATCHER, EXTRACTOR, BOOLEAN, INT, LONG, FLOAT, DOUBLE, STRING);
  }

  interface Enums {
    Enum<BoundType> BOUND_TYPE = new EnumImpl<>("BoundType",
    Arrays.stream(BoundType.values())
    .map(java.lang.Enum::name)
    .toList());

    Enum<Primitive> PRIMITIVE = new EnumImpl<>("Primitive",
    Arrays.stream(Primitive.values())
    .map(java.lang.Enum::name)
    .toList());

    List<Enum<?>> ALL = List.of(BOUND_TYPE, PRIMITIVE);
  }

  interface Aliases {
    Alias DEFINITION = new AliasImpl("Definition", "Group concrete");

    Alias PLUS_CONTAINS = new AliasImpl("+contains", "Relation contains immutable=false");

    Alias MINUS_CONTAINS = new AliasImpl("-contains", "Relation contains immutable");

    Alias PLUS_REFERS = new AliasImpl("+refers", "Relation contains=false immutable=false");

    Alias MINUS_REFERS = new AliasImpl("-refers", "Relation contains=false immutable");

    Alias PLUS_ATT = new AliasImpl("+att", "Attribute immutable=false");

    Alias MINUS_ATT = new AliasImpl("-att", "Attribute immutable");

    Alias LSB_0_DOT_DOT_1_RSB = new AliasImpl("[0..1]", "mandatory=false many=false");

    Alias LSB_1_DOT_DOT_1_RSB = new AliasImpl("[1..1]", "mandatory many=false");

    Alias LSB_0_DOT_DOT_STAR_RSB = new AliasImpl("[0..*]", "mandatory=false many");

    Alias LSB_1_DOT_DOT_STAR_RSB = new AliasImpl("[1..*]", "mandatory many");

    List<Alias> ALL = List.of(DEFINITION, PLUS_CONTAINS, MINUS_CONTAINS, PLUS_REFERS, MINUS_REFERS, PLUS_ATT, MINUS_ATT, LSB_0_DOT_DOT_1_RSB, LSB_1_DOT_DOT_1_RSB, LSB_0_DOT_DOT_STAR_RSB, LSB_1_DOT_DOT_STAR_RSB);
  }

  interface JavaWrappers {
    JavaWrapper<RawFeature<?, ?>> RAW_FEATURE = new JavaWrapperImpl<>("RawFeature", "org.logoce.lmf.model.api.feature");

    JavaWrapper<IModelPackage> I_MODEL_PACKAGE = new JavaWrapperImpl<>("IModelPackage", "org.logoce.lmf.model.api.model");

    List<JavaWrapper<?>> ALL = List.of(RAW_FEATURE, I_MODEL_PACKAGE);
  }
}
