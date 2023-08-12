package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.RelationImpl;

import java.util.List;

public interface LMCoreFeatures
{
	Attribute<String, String> Named_name = new AttributeImpl<>("name", true, false, true, LMCorePackage.STRING_UNIT);

	List<Feature<?, ?>> Named_all = List.of(Named_name);

	Attribute<String, String> Type_name = Named_name;

	List<Feature<?, ?>> Type_all = List.of(Type_name);

	Attribute<String, String> Enum_name = Named_name;
	Attribute<String, List<String>> Enum_literals = new AttributeImpl<>("literals",
																		true,
																		true,
																		false,
																		LMCorePackage.STRING_UNIT);

	List<Feature<?, ?>> Enum_all = List.of(Enum_name, Enum_literals);

	Attribute<String, String> Generic_name = Named_name;
	Relation<Type, Type> Generic_type = new RelationImpl<>("type",
														   true,
														   false,
														   true,
														   LMCorePackage.TYPE_GROUP,
														   false,
														   null);
	Attribute<BoundType, BoundType> Generic_boundType = new AttributeImpl<>("boundType",
																			true,
																			false,
																			false,
																			LMCorePackage.BOUND_TYPE_ENUM);

	List<Feature<?, ?>> Generic_all = List.of(Generic_name, Generic_type, Generic_type);

	Attribute<String, String> Feature_name = Named_name;
	Attribute<Boolean, Boolean> Feature_immutable = new AttributeImpl<>("immutable",
																		true,
																		false,
																		false,
																		LMCorePackage.BOOLEAN_UNIT);
	Attribute<Boolean, Boolean> Feature_many = new AttributeImpl<>("many",
																   true,
																   false,
																   false,
																   LMCorePackage.BOOLEAN_UNIT);
	Attribute<Boolean, Boolean> Feature_mandatory = new AttributeImpl<>("mandatory",
																		true,
																		false,
																		false,
																		LMCorePackage.BOOLEAN_UNIT);

	List<Feature<?, ?>> Feature_all = List.of(Feature_name, Feature_immutable, Feature_many, Feature_mandatory);

	Attribute<String, String> Attribute_name = Named_name;
	Attribute<Boolean, Boolean> Attribute_immutable = Feature_immutable;
	Attribute<Boolean, Boolean> Attribute_many = Feature_many;
	Attribute<Boolean, Boolean> Attribute_mandatory = Feature_mandatory;
	Relation<Datatype<?>, Datatype<?>> Attribute_datatype = new RelationImpl<>("datatype",
																			   true,
																			   false,
																			   true,
																			   LMCorePackage.DATATYPE_GROUP,
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
	Attribute<String, String> Unit_matcher = new AttributeImpl<>("matcher",
																 true,
																 false,
																 false,
																 LMCorePackage.MATCHER_UNIT);
	Attribute<String, String> Unit_defaultValue = new AttributeImpl<>("defaultValue",
																	  true,
																	  false,
																	  false,
																	  LMCorePackage.STRING_UNIT);
	Attribute<Primitive, Primitive> Unit_primitive = new AttributeImpl<>("primitive",
																		 true,
																		 false,
																		 true,
																		 LMCorePackage.PRIMITIVE_ENUM);
	Attribute<String, String> Unit_extractor = new AttributeImpl<>("extractor",
																   true,
																   false,
																   false,
																   LMCorePackage.EXTRACTOR_UNIT);

	List<Feature<?, ?>> Unit_all = List.of(Unit_name, Unit_matcher, Unit_defaultValue, Unit_primitive, Unit_extractor);

	Attribute<String, String> Alias_name = Named_name;
	Attribute<String, List<String>> Alias_words = new AttributeImpl<>("words",
																	  true,
																	  true,
																	  false,
																	  LMCorePackage.STRING_UNIT);

	List<Feature<?, ?>> Alias_all = List.of(Alias_name, Alias_words);
	Attribute<String, String> Group_name = Named_name;
	Attribute<Boolean, Boolean> Group_concrete = new AttributeImpl<>("concrete",
																	 true,
																	 false,
																	 false,
																	 LMCorePackage.BOOLEAN_UNIT);
	Relation<Group<?>, List<? extends Group<?>>> Group_includes = new RelationImpl<>("includes",
																					 true,
																					 true,
																					 false,
																					 LMCorePackage.GROUP_GROUP,
																					 false,
																					 null);
	Relation<Feature<?, ?>, List<? extends Feature<?, ?>>> Group_features = new RelationImpl<>("features",
																							   true,
																							   true,
																							   false,
																							   LMCorePackage.FEATURE_GROUP,
																							   true,
																							   null);
	Relation<Generic, List<Generic>> Group_generics = new RelationImpl<>("generics",
																		 true,
																		 true,
																		 false,
																		 LMCorePackage.GENERIC_GROUP,
																		 true,
																		 null);

	List<Feature<?, ?>> Group_all = List.of(Group_name, Group_concrete, Group_includes, Group_features, Group_generics);

	Attribute<String, String> Relation_name = Named_name;
	Attribute<Boolean, Boolean> Relation_immutable = Feature_immutable;
	Attribute<Boolean, Boolean> Relation_many = Feature_many;
	Attribute<Boolean, Boolean> Relation_mandatory = Feature_mandatory;
	Relation<Group<?>, Group<?>> Relation_group = new RelationImpl<>("group",
																	 true,
																	 false,
																	 true,
																	 LMCorePackage.GROUP_GROUP,
																	 false,
																	 null);
	Attribute<Boolean, Boolean> Relation_contains = new AttributeImpl<>("contains",
																		true,
																		false,
																		false,
																		LMCorePackage.BOOLEAN_UNIT);
	Relation<Generic, Generic> Relation_parameter = new RelationImpl<>("parameter",
																	   true,
																	   false,
																	   false,
																	   LMCorePackage.GENERIC_GROUP,
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
																		 LMCorePackage.GROUP_GROUP,
																		 true,
																		 null);
	Relation<Enum<?>, List<Enum<?>>> Model_enums = new RelationImpl<>("enums",
																	  true,
																	  true,
																	  false,
																	  LMCorePackage.ENUM_GROUP,
																	  true,
																	  null);
	Relation<Unit<?>, List<Unit<?>>> Model_units = new RelationImpl<>("units",
																	  true,
																	  true,
																	  false,
																	  LMCorePackage.UNIT_GROUP,
																	  true,
																	  null);
	Relation<Alias, List<Alias>> Model_aliases = new RelationImpl<>("aliases",
																	true,
																	true,
																	false,
																	LMCorePackage.ALIAS_GROUP,
																	true,
																	null);

	List<Feature<?, ?>> Model_all = List.of(Model_name, Model_groups, Model_enums, Model_units, Model_aliases);
}
