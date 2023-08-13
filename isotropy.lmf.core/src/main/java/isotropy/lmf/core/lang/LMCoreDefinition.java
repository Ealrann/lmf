package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.*;

import java.util.Arrays;
import java.util.List;

public interface LMCoreDefinition
{
	interface Features
	{
		Attribute<String, String> Named_name = new AttributeImpl<>("name", true, false, true, Units.STRING);

		List<Feature<?, ?>> Named_all = List.of(Named_name);

		Attribute<String, String> Type_name = Named_name;

		List<Feature<?, ?>> Type_all = List.of(Type_name);

		Attribute<String, String> Enum_name = Named_name;
		Attribute<String, List<String>> Enum_literals = new AttributeImpl<>("literals",
																			true,
																			true,
																			false,
																			Units.STRING);

		List<Feature<?, ?>> Enum_all = List.of(Enum_name, Enum_literals);

		Attribute<String, String> Generic_name = Named_name;
		Relation<Type, Type> Generic_type = new RelationImpl<>("type",
															   true,
															   false,
															   true,
															   Groups.TYPE,
															   false,
															   null);
		Attribute<BoundType, BoundType> Generic_boundType = new AttributeImpl<>("boundType",
																				true,
																				false,
																				false,
																				Enums.BOUND_TYPE);

		List<Feature<?, ?>> Generic_all = List.of(Generic_name, Generic_type, Generic_boundType);

		Attribute<String, String> Feature_name = Named_name;
		Attribute<Boolean, Boolean> Feature_immutable = new AttributeImpl<>("immutable",
																			true,
																			false,
																			false,
																			Units.BOOLEAN);
		Attribute<Boolean, Boolean> Feature_many = new AttributeImpl<>("many", true, false, false, Units.BOOLEAN);
		Attribute<Boolean, Boolean> Feature_mandatory = new AttributeImpl<>("mandatory",
																			true,
																			false,
																			false,
																			Units.BOOLEAN);

		List<Feature<?, ?>> Feature_all = List.of(Feature_name, Feature_immutable, Feature_many, Feature_mandatory);

		Attribute<String, String> Attribute_name = Named_name;
		Attribute<Boolean, Boolean> Attribute_immutable = Feature_immutable;
		Attribute<Boolean, Boolean> Attribute_many = Feature_many;
		Attribute<Boolean, Boolean> Attribute_mandatory = Feature_mandatory;
		Relation<Datatype<?>, Datatype<?>> Attribute_datatype = new RelationImpl<>("datatype",
																				   true,
																				   false,
																				   true,
																				   Groups.DATATYPE,
																				   false,
																				   null);

		List<Feature<?, ?>> Attribute_all = List.of(Attribute_name,
													Attribute_immutable,
													Attribute_many,
													Attribute_mandatory,
													Attribute_datatype);

		Attribute<String, String> Datatype_name = Type_name;

		List<Feature<?, ?>> Datatype_all = List.of(Datatype_name);

		Attribute<String, String> Unit_name = Named_name;
		Attribute<String, String> Unit_matcher = new AttributeImpl<>("matcher", true, false, false, Units.MATCHER);
		Attribute<String, String> Unit_defaultValue = new AttributeImpl<>("defaultValue",
																		  true,
																		  false,
																		  false,
																		  Units.STRING);
		Attribute<Primitive, Primitive> Unit_primitive = new AttributeImpl<>("primitive",
																			 true,
																			 false,
																			 true,
																			 Enums.PRIMITIVE);
		Attribute<String, String> Unit_extractor = new AttributeImpl<>("extractor",
																	   true,
																	   false,
																	   false,
																	   Units.EXTRACTOR);

		List<Feature<?, ?>> Unit_all = List.of(Unit_name,
											   Unit_matcher,
											   Unit_defaultValue,
											   Unit_primitive,
											   Unit_extractor);

		Attribute<String, String> Alias_name = Named_name;
		Attribute<String, List<String>> Alias_words = new AttributeImpl<>("words",
																		  true,
																		  true,
																		  false,
																		  Units.STRING);

		List<Feature<?, ?>> Alias_all = List.of(Alias_name, Alias_words);
		Attribute<String, String> Group_name = Named_name;
		Attribute<Boolean, Boolean> Group_concrete = new AttributeImpl<>("concrete",
																		 true,
																		 false,
																		 false,
																		 Units.BOOLEAN);
		Relation<Group<?>, List<? extends Group<?>>> Group_includes = new RelationImpl<>("includes",
																						 true,
																						 true,
																						 false,
																						 Groups.GROUP,
																						 false,
																						 null);
		Relation<Feature<?, ?>, List<? extends Feature<?, ?>>> Group_features = new RelationImpl<>("features",
																								   true,
																								   true,
																								   false,
																								   Groups.FEATURE,
																								   true,
																								   null);
		Relation<Generic, List<Generic>> Group_generics = new RelationImpl<>("generics",
																			 true,
																			 true,
																			 false,
																			 Groups.GENERIC,
																			 true,
																			 null);

		List<Feature<?, ?>> Group_all = List.of(Group_name,
												Group_concrete,
												Group_includes,
												Group_features,
												Group_generics);

		Attribute<String, String> Relation_name = Named_name;
		Attribute<Boolean, Boolean> Relation_immutable = Feature_immutable;
		Attribute<Boolean, Boolean> Relation_many = Feature_many;
		Attribute<Boolean, Boolean> Relation_mandatory = Feature_mandatory;
		Relation<Group<?>, Group<?>> Relation_group = new RelationImpl<>("group",
																		 true,
																		 false,
																		 true,
																		 Groups.GROUP,
																		 false,
																		 null);
		Attribute<Boolean, Boolean> Relation_contains = new AttributeImpl<>("contains",
																			true,
																			false,
																			false,
																			Units.BOOLEAN);
		Relation<Generic, Generic> Relation_parameter = new RelationImpl<>("parameter",
																		   true,
																		   false,
																		   false,
																		   Groups.GENERIC,
																		   false,
																		   null);

		List<Feature<?, ?>> Relation_all = List.of(Relation_name,
												   Relation_immutable,
												   Relation_many,
												   Relation_mandatory,
												   Relation_group,
												   Relation_contains,
												   Relation_parameter);

		Attribute<String, String> Model_name = Named_name;
		Relation<Group<?>, List<Group<?>>> Model_groups = new RelationImpl<>("groups",
																			 true,
																			 true,
																			 false,
																			 Groups.GROUP,
																			 true,
																			 null);
		Relation<Enum<?>, List<Enum<?>>> Model_enums = new RelationImpl<>("enums",
																		  true,
																		  true,
																		  false,
																		  Groups.ENUM,
																		  true,
																		  null);
		Relation<Unit<?>, List<Unit<?>>> Model_units = new RelationImpl<>("units",
																		  true,
																		  true,
																		  false,
																		  Groups.UNIT,
																		  true,
																		  null);
		Relation<Alias, List<Alias>> Model_aliases = new RelationImpl<>("aliases",
																		true,
																		true,
																		false,
																		Groups.ALIAS,
																		true,
																		null);

		List<Feature<?, ?>> Model_all = List.of(Model_name, Model_groups, Model_enums, Model_units, Model_aliases);
	}

	interface Groups
	{
		Group<LMObject> LM_OBJECT = new GroupImpl<>("LMObject", false, List.of(), List.of(), List.of());
		Group<Named> NAMED = new GroupImpl<>("Named",
											 false,
											 List.of(LM_OBJECT),
											 Features.Named_all,
											 List.of());
		Group<Type> TYPE = new GroupImpl<>("Type", false, List.of(NAMED), Features.Type_all, List.of());
		Group<Model> MODEL = new GroupImpl<>("Model", true, List.of(NAMED), Features.Model_all, List.of());
		Group<Group<?>> GROUP = new GroupImpl<>("Group",
												true,
												List.of(TYPE),
												Features.Group_all,
												Group.GENERICS);
		Group<Feature<?, ?>> FEATURE = new GroupImpl<>("Feature",
													   false,
													   List.of(NAMED),
													   Features.Feature_all,
													   Feature.GENERICS);
		Group<Attribute<?, ?>> ATTRIBUTE = new GroupImpl<>("Attribute",
														   true,
														   List.of(FEATURE),
														   Features.Attribute_all,
														   Attribute.GENERICS);
		Group<Relation<?, ?>> RELATION = new GroupImpl<>("Relation",
														 true,
														 List.of(FEATURE),
														 Features.Relation_all,
														 Relation.GENERICS);
		Group<Datatype<?>> DATATYPE = new GroupImpl<>("Datatype",
													  false,
													  List.of(TYPE),
													  Features.Datatype_all,
													  Datatype.GENERICS);
		Group<Alias> ALIAS = new GroupImpl<>("Alias", true, List.of(NAMED), Features.Alias_all, List.of());
		Group<Enum<?>> ENUM = new GroupImpl<>("Enum",
											  true,
											  List.of(DATATYPE),
											  Features.Enum_all,
											  Enum.GENERICS);
		Group<Unit<?>> UNIT = new GroupImpl<>("Unit",
											  true,
											  List.of(DATATYPE),
											  Features.Unit_all,
											  Unit.GENERICS);
		Group<Generic> GENERIC = new GroupImpl<>("Generic",
												 true,
												 List.of(NAMED),
												 Features.Generic_all,
												 List.of());
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
