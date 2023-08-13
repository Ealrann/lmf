package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.*;

import java.util.Arrays;
import java.util.List;

public interface LMCoreDefinition
{
	interface Features
	{
		interface NAMED
		{
			Attribute<String, String> name = new AttributeImpl<>("name", true, false, true, Units.STRING);
			List<Feature<?, ?>> all = List.of(name);
		}

		interface TYPE
		{
			Attribute<String, String> name = NAMED.name;
			List<Feature<?, ?>> all = List.of(name);
		}

		interface ENUM
		{
			Attribute<String, String> name = NAMED.name;
			Attribute<String, List<String>> literals = new AttributeImpl<>("literals", true, true, false, Units.STRING);
			List<Feature<?, ?>> all = List.of(name, literals);
		}

		interface GENERIC
		{
			Attribute<String, String> name = NAMED.name;
			Relation<Type, Type> type = new RelationImpl<>("type", true, false, true, Groups.TYPE, false, null);
			Attribute<BoundType, BoundType> boundType = new AttributeImpl<>("boundType",
																			true,
																			false,
																			false,
																			Enums.BOUND_TYPE);
			List<Feature<?, ?>> all = List.of(name, type, boundType);
		}

		interface FEATURE
		{
			Attribute<String, String> name = NAMED.name;
			Attribute<Boolean, Boolean> immutable = new AttributeImpl<>("immutable", true, false, false, Units.BOOLEAN);
			Attribute<Boolean, Boolean> many = new AttributeImpl<>("many", true, false, false, Units.BOOLEAN);
			Attribute<Boolean, Boolean> mandatory = new AttributeImpl<>("mandatory", true, false, false, Units.BOOLEAN);
			List<Feature<?, ?>> all = List.of(name, immutable, many, mandatory);
		}

		interface ATTRIBUTE
		{
			Attribute<String, String> name = FEATURE.name;
			Attribute<Boolean, Boolean> immutable = FEATURE.immutable;
			Attribute<Boolean, Boolean> many = FEATURE.many;
			Attribute<Boolean, Boolean> mandatory = FEATURE.mandatory;
			Relation<Datatype<?>, Datatype<?>> datatype = new RelationImpl<>("datatype",
																			 true,
																			 false,
																			 true,
																			 Groups.DATATYPE,
																			 false,
																			 null);
			List<Feature<?, ?>> all = List.of(name, immutable, many, mandatory, datatype);
		}

		interface DATA_TYPE
		{
			Attribute<String, String> name = NAMED.name;
			List<Feature<?, ?>> all = List.of(name);
		}

		interface UNIT
		{
			Attribute<String, String> name = NAMED.name;
			Attribute<String, String> matcher = new AttributeImpl<>("matcher", true, false, false, Units.MATCHER);
			Attribute<String, String> defaultValue = new AttributeImpl<>("defaultValue",
																		 true,
																		 false,
																		 false,
																		 Units.STRING);
			Attribute<Primitive, Primitive> primitive = new AttributeImpl<>("primitive",
																			true,
																			false,
																			true,
																			Enums.PRIMITIVE);
			Attribute<String, String> extractor = new AttributeImpl<>("extractor", true, false, false, Units.EXTRACTOR);
			List<Feature<?, ?>> all = List.of(name, matcher, defaultValue, primitive, extractor);
		}

		interface ALIAS
		{
			Attribute<String, String> name = NAMED.name;
			Attribute<String, List<String>> words = new AttributeImpl<>("words", true, true, false, Units.STRING);

			List<Feature<?, ?>> all = List.of(name, words);
		}

		interface GROUP
		{
			Attribute<String, String> name = NAMED.name;
			Attribute<Boolean, Boolean> concrete = new AttributeImpl<>("concrete", true, false, false, Units.BOOLEAN);
			Relation<Group<?>, List<? extends Group<?>>> includes = new RelationImpl<>("includes",
																					   true,
																					   true,
																					   false,
																					   Groups.GROUP,
																					   false,
																					   null);
			Relation<Feature<?, ?>, List<? extends Feature<?, ?>>> features = new RelationImpl<>("features",
																								 true,
																								 true,
																								 false,
																								 Groups.FEATURE,
																								 true,
																								 null);
			Relation<Generic, List<Generic>> generics = new RelationImpl<>("generics",
																		   true,
																		   true,
																		   false,
																		   Groups.GENERIC,
																		   true,
																		   null);
			List<Feature<?, ?>> all = List.of(name, concrete, includes, features, generics);
		}

		interface RELATION
		{
			Attribute<String, String> name = FEATURE.name;
			Attribute<Boolean, Boolean> immutable = FEATURE.immutable;
			Attribute<Boolean, Boolean> many = FEATURE.many;
			Attribute<Boolean, Boolean> mandatory = FEATURE.mandatory;
			Relation<Group<?>, Group<?>> group = new RelationImpl<>("group",
																	true,
																	false,
																	true,
																	Groups.GROUP,
																	false,
																	null);
			Attribute<Boolean, Boolean> contains = new AttributeImpl<>("contains", true, false, false, Units.BOOLEAN);
			Relation<Generic, Generic> parameter = new RelationImpl<>("parameter",
																	  true,
																	  false,
																	  false,
																	  Groups.GENERIC,
																	  false,
																	  null);
			List<Feature<?, ?>> all = List.of(name, immutable, many, mandatory, group, contains, parameter);
		}

		interface MODEL
		{
			Attribute<String, String> name = NAMED.name;
			Relation<Group<?>, List<Group<?>>> groups = new RelationImpl<>("groups",
																		   true,
																		   true,
																		   false,
																		   Groups.GROUP,
																		   true,
																		   null);
			Relation<Enum<?>, List<Enum<?>>> enums = new RelationImpl<>("enums",
																		true,
																		true,
																		false,
																		Groups.ENUM,
																		true,
																		null);
			Relation<Unit<?>, List<Unit<?>>> units = new RelationImpl<>("units",
																		true,
																		true,
																		false,
																		Groups.UNIT,
																		true,
																		null);
			Relation<Alias, List<Alias>> aliases = new RelationImpl<>("aliases",
																	  true,
																	  true,
																	  false,
																	  Groups.ALIAS,
																	  true,
																	  null);
			List<Feature<?, ?>> all = List.of(name, groups, enums, units, aliases);
		}
	}

	interface Groups
	{
		Group<LMObject> LM_OBJECT = new GroupImpl<>("LMObject", false, List.of(), List.of(), List.of());
		Group<Named> NAMED = new GroupImpl<>("Named", false, List.of(LM_OBJECT), Features.NAMED.all, List.of());
		Group<Type> TYPE = new GroupImpl<>("Type", false, List.of(NAMED), Features.TYPE.all, List.of());
		Group<Model> MODEL = new GroupImpl<>("Model", true, List.of(NAMED), Features.MODEL.all, List.of());
		Group<Group<?>> GROUP = new GroupImpl<>("Group", true, List.of(TYPE), Features.GROUP.all, Group.GENERICS);
		Group<Feature<?, ?>> FEATURE = new GroupImpl<>("Feature",
													   false,
													   List.of(NAMED),
													   Features.FEATURE.all,
													   Feature.GENERICS);
		Group<Attribute<?, ?>> ATTRIBUTE = new GroupImpl<>("Attribute",
														   true,
														   List.of(FEATURE),
														   Features.ATTRIBUTE.all,
														   Attribute.GENERICS);
		Group<Relation<?, ?>> RELATION = new GroupImpl<>("Relation",
														 true,
														 List.of(FEATURE),
														 Features.RELATION.all,
														 Relation.GENERICS);
		Group<Datatype<?>> DATATYPE = new GroupImpl<>("Datatype",
													  false,
													  List.of(TYPE),
													  Features.DATA_TYPE.all,
													  Datatype.GENERICS);
		Group<Alias> ALIAS = new GroupImpl<>("Alias", true, List.of(NAMED), Features.ALIAS.all, List.of());
		Group<Enum<?>> ENUM = new GroupImpl<>("Enum", true, List.of(DATATYPE), Features.ENUM.all, Enum.GENERICS);
		Group<Unit<?>> UNIT = new GroupImpl<>("Unit", true, List.of(DATATYPE), Features.UNIT.all, Unit.GENERICS);
		Group<Generic> GENERIC = new GroupImpl<>("Generic", true, List.of(NAMED), Features.GENERIC.all, List.of());
	}

	interface Units
	{
		Unit<String> MATCHER = new UnitImpl<>("matcher",
											  "rgx_match:\\b(rgx_match:)*+\\b",
											  null,
											  Primitive.String,
											  null);
		Unit<String> EXTRACTOR = new UnitImpl<>("extractor",
												"rgx_match:\\b(rgx_value:)*+\\b",
												null,
												Primitive.String,
												null);
		Unit<Boolean> BOOLEAN = new UnitImpl<>("boolean", "\\b(true|false)\\b", "false", Primitive.Boolean, null);
		Unit<String> INT = new UnitImpl<>("int", null, null, Primitive.Int, null);
		Unit<String> LONG = new UnitImpl<>("long", null, null, Primitive.Long, null);
		Unit<String> FLOAT = new UnitImpl<>("float", null, null, Primitive.Float, null);
		Unit<String> DOUBLE = new UnitImpl<>("double", null, null, Primitive.Double, null);
		Unit<String> STRING = new UnitImpl<>("string", null, null, Primitive.String, null);
	}

	interface Enums
	{
		Enum<BoundType> BOUND_TYPE = new EnumImpl<>("boundType",
													Arrays.stream(BoundType.values())
														  .map(java.lang.Enum::name)
														  .toList());
		Enum<Primitive> PRIMITIVE = new EnumImpl<>("primitive",
												   Arrays.stream(Primitive.values())
														 .map(java.lang.Enum::name)
														 .toList());
	}

	interface Aliases
	{
		Alias DEFINITION = new AliasImpl("Definition", List.of("Group", "concrete"));
		Alias PLUS_CONTAINS = new AliasImpl("+contains", List.of("Relation", "contains", "immutable=false"));
		Alias MINUS_CONTAINS = new AliasImpl("-contains", List.of("Relation", "contains", "immutable"));
		Alias PLUS_REFERS = new AliasImpl("+refers", List.of("Relation", "contains=false", "immutable=false"));
		Alias MINUS_REFERS = new AliasImpl("-refers", List.of("Relation", "contains=false", "immutable"));
		Alias PLUS_ATT = new AliasImpl("+att", List.of("Attribute", "immutable=false"));
		Alias MINUS_ATT = new AliasImpl("-att", List.of("Attribute", "immutable"));
		Alias LB_0_DOT_DOT_1_RB = new AliasImpl("[0..1]", List.of("mandatory=false", "many=false"));
		Alias LB_1_DOT_DOT_1_RB = new AliasImpl("[1..1]", List.of("mandatory", "many=false"));
		Alias LB_0_DOT_DOT_STAR_RB = new AliasImpl("[0..*]", List.of("mandatory=false", "many"));
		Alias LB_1_DOT_DOT_STAR_RB = new AliasImpl("[1..*]", List.of("mandatory", "many"));
	}
}
