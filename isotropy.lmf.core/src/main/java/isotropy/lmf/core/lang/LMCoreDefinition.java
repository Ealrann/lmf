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
			Relation<Type<?>, Type<?>> type = new RelationImpl<>("type",
																 true,
																 false,
																 true,
																 new ReferenceImpl<>(() -> Groups.TYPE, List.of()),
																 false);
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
																			 new ReferenceImpl<>(() -> Groups.DATATYPE,
																								 List.of()),
																			 false);
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
			Relation<Reference<?>, List<? extends Reference<?>>> includes = new RelationImpl<>("includes",
																							   true,
																							   true,
																							   false,
																							   new ReferenceImpl<>(() -> Groups.REFERENCE,
																												   List.of()),
																							   true);
			Relation<Feature<?, ?>, List<? extends Feature<?, ?>>> features = new RelationImpl<>("features",
																								 true,
																								 true,
																								 false,
																								 new ReferenceImpl<>(() -> Groups.FEATURE,
																													 List.of()),
																								 true);
			Relation<Generic<?>, List<? extends Generic<?>>> generics = new RelationImpl<>("generics",
																						   true,
																						   true,
																						   false,
																						   new ReferenceImpl<>(() -> Groups.GENERIC,
																											   List.of()),
																						   true);
			Relation<Generic<?>, List<? extends Generic<?>>> parameters = new RelationImpl<>("parameters",
																							 true,
																							 true,
																							 false,
																							 new ReferenceImpl<>(() -> Groups.GENERIC,
																												 List.of()),
																							 false);
			List<Feature<?, ?>> all = List.of(name, concrete, includes, features, generics, parameters);
		}

		interface RELATION
		{
			Attribute<String, String> name = FEATURE.name;
			Attribute<Boolean, Boolean> immutable = FEATURE.immutable;
			Attribute<Boolean, Boolean> many = FEATURE.many;
			Attribute<Boolean, Boolean> mandatory = FEATURE.mandatory;
			Relation<Reference<?>, Reference<?>> reference = new RelationImpl<>("reference",
																				true,
																				false,
																				true,
																				new ReferenceImpl<>(() -> Groups.REFERENCE,
																									List.of(() -> Groups.RELATION.generics().get(0))),
																				true);
			Attribute<Boolean, Boolean> contains = new AttributeImpl<>("contains", true, false, false, Units.BOOLEAN);

			List<Feature<?, ?>> all = List.of(name, immutable, many, mandatory, reference, contains);
		}

		interface MODEL
		{
			Attribute<String, String> name = NAMED.name;
			Attribute<String, String> domain = new AttributeImpl<>("domain",
																   true,
																   false,
																   true,
																   Units.STRING);
			Relation<Group<?>, List<Group<?>>> groups = new RelationImpl<>("groups",
																		   true,
																		   true,
																		   false,
																		   new ReferenceImpl<>(() -> Groups.GROUP,
																							   List.of()),
																		   true);
			Relation<Enum<?>, List<Enum<?>>> enums = new RelationImpl<>("enums",
																		true,
																		true,
																		false,
																		new ReferenceImpl<>(() -> Groups.ENUM,
																							List.of()),
																		true);
			Relation<Unit<?>, List<Unit<?>>> units = new RelationImpl<>("units",
																		true,
																		true,
																		false,
																		new ReferenceImpl<>(() -> Groups.UNIT,
																							List.of()),
																		true);
			Relation<Alias, List<Alias>> aliases = new RelationImpl<>("aliases",
																	  true,
																	  true,
																	  false,
																	  new ReferenceImpl<>(() -> Groups.ALIAS, List.of()),
																	  true);
			List<Feature<?, ?>> all = List.of(name, domain, groups, enums, units, aliases);
		}

		interface REFERENCE
		{
			Relation<Concept<?>, Concept<?>> group = new RelationImpl<>("group",
																		true,
																		false,
																		true,
																		new ReferenceImpl<>(() -> Groups.CONCEPT,
																							List.of()),
																		false);
			Relation<Concept<?>, List<Concept<?>>> parameters = new RelationImpl<>("parameters",
																				   true,
																				   true,
																				   false,
																				   new ReferenceImpl<>(() -> Groups.CONCEPT,
																									   List.of()),
																				   false);
			List<Feature<?, ?>> all = List.of(group, parameters);
		}

		interface GENERIC_REFERENCE
		{
			Relation<Generic<?>, List<Generic<?>>> generic = new RelationImpl<>("generic",
																				true,
																				false,
																				false,
																				new ReferenceImpl<>(() -> Groups.GENERIC,
																									List.of()),
																				false);
			List<Feature<?, ?>> all = List.of(generic);
		}

		interface PARAMETER
		{
			Relation<Generic<?>, List<Generic<?>>> generic = new RelationImpl<>("generic",
																				true,
																				false,
																				false,
																				new ReferenceImpl<>(() -> Groups.GENERIC,
																									List.of()),
																				false);
			List<Feature<?, ?>> all = List.of(generic);
		}
	}

	interface Groups
	{
		Group<LMObject> LM_OBJECT = new GroupImpl<>("LMObject", false, List.of(), List.of(), List.of(), List.of());
		Group<Named> NAMED = new GroupImpl<>("Named",
											 false,
											 List.of(new ReferenceImpl<>(() -> LM_OBJECT, List.of())),
											 Features.NAMED.all,
											 List.of(),
											 List.of());
		Group<Type<?>> TYPE = new GroupImpl<>("Type",
											  false,
											  List.of(new ReferenceImpl<>(() -> NAMED, List.of())),
											  Features.TYPE.all,
											  List.of(),
											  List.of());

		Group<Concept<?>> CONCEPT = new GroupImpl<>("Concept",
													false,
													List.of(new ReferenceImpl<>(() -> NAMED, List.of())),
													Features.TYPE.all,
													List.of(),
													List.of());
		Group<Model> MODEL = new GroupImpl<>("Model",
											 true,
											 List.of(new ReferenceImpl<>(() -> NAMED, List.of())),
											 Features.MODEL.all,
											 List.of(),
											 List.of());
		Group<Group<?>> GROUP = new GroupImpl<>("Group",
												true,
												List.of(new ReferenceImpl<>(() -> TYPE, List.of()),
														new ReferenceImpl<>(() -> CONCEPT, List.of())),
												Features.GROUP.all,
												Group.GENERICS,
												Group.GENERICS);
		Group<Feature<?, ?>> FEATURE = new GroupImpl<>("Feature",
													   false,
													   List.of(new ReferenceImpl<>(() -> NAMED, List.of())),
													   Features.FEATURE.all,
													   Feature.GENERICS,
													   Feature.GENERICS);
		Group<Attribute<?, ?>> ATTRIBUTE = new GroupImpl<>("Attribute",
														   true,
														   List.of(new ReferenceImpl<>(() -> FEATURE, List.of())),
														   Features.ATTRIBUTE.all,
														   Generics.Attribute,
														   Generics.Attribute);
		Group<Relation<?, ?>> RELATION = new GroupImpl<>("Relation",
														 true,
														 List.of(new ReferenceImpl<>(() -> FEATURE,
																					 List.of(() -> Generics.Relation.get(0),
																							 () -> Generics.Relation.get(1)))),
														 Features.RELATION.all,
														 Generics.Relation,
														 Generics.Relation);
		Group<Datatype<?>> DATATYPE = new GroupImpl<>("Datatype",
													  false,
													  List.of(new ReferenceImpl<>(() -> TYPE, List.of())),
													  Features.DATA_TYPE.all,
													  Generics.DataType,
													  Generics.DataType);
		Group<Alias> ALIAS = new GroupImpl<>("Alias",
											 true,
											 List.of(new ReferenceImpl<>(() -> NAMED, List.of())),
											 Features.ALIAS.all,
											 List.of(),
											 List.of());
		Group<Enum<?>> ENUM = new GroupImpl<>("Enum",
											  true,
											  List.of(new ReferenceImpl<>(() -> DATATYPE, List.of())),
											  Features.ENUM.all,
											  Generics.Enum,
											  Generics.Enum);
		Group<Unit<?>> UNIT = new GroupImpl<>("Unit",
											  true,
											  List.of(new ReferenceImpl<>(() -> DATATYPE, List.of())),
											  Features.UNIT.all,
											  Generics.Unit,
											  Generics.Unit);
		Group<Generic<?>> GENERIC = new GroupImpl<>("Generic",
													true,
													List.of(new ReferenceImpl<>(() -> CONCEPT, List.of())),
													Features.GENERIC.all,
													List.of(),
													List.of());

		Group<Reference<?>> REFERENCE = new GroupImpl<>("Reference",
														true,
														List.of(new ReferenceImpl<>(() -> LM_OBJECT, List.of())),
														Features.REFERENCE.all,
														Generics.Reference,
														Generics.Reference);
	}

	interface Units
	{
		Unit<String> MATCHER = new UnitImpl<>("matcher", "rgx_match:<(.+?)>", null, Primitive.String, null);
		Unit<String> EXTRACTOR = new UnitImpl<>("extractor", "rgx_match:<(.+?)>", null, Primitive.String, null);
		Unit<Boolean> BOOLEAN = new UnitImpl<>("boolean", "rgx_match:<(true|false)>", "false", Primitive.Boolean, null);
		Unit<String> INT = new UnitImpl<>("int", "rgx_match:<[0-9]+>", null, Primitive.Int, null);
		Unit<String> LONG = new UnitImpl<>("long", "rgx_match:<[0-9]+[Ll]>", null, Primitive.Long, null);
		Unit<String> FLOAT = new UnitImpl<>("float", "rgx_match:<[0-9.]+[Ff]>", null, Primitive.Float, null);
		Unit<String> DOUBLE = new UnitImpl<>("double", "rgx_match:<[0-9.]+>", null, Primitive.Double, null);
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
		Alias DREF = new AliasImpl("dref", List.of("reference=DirectReference"));
		Alias GREF = new AliasImpl("gref", List.of("reference=GenericReference"));
	}

	interface Generics
	{
		List<Generic<?>> Attribute = List.of(new GenericImpl<>("UnaryType", null, null),
											 new GenericImpl<>("EffectiveType", null, null));
		List<Generic<?>> DataType = List.of(new GenericImpl<>("T", null, null));
		List<Generic<?>> Enum = List.of(new GenericImpl<>("T", null, null));
		List<Generic<?>> Relation = List.of(new GenericImpl<>("UnaryType", BoundType.Extends, Groups.LM_OBJECT),
											new GenericImpl<>("EffectiveType", null, null));
		List<Generic<?>> Unit = List.of(new GenericImpl<>("T", null, null));
		List<Generic<?>> Reference = List.of(new GenericImpl<>("T",
															   BoundType.Extends,
															   LMCoreDefinition.Groups.LM_OBJECT));
	}
}
