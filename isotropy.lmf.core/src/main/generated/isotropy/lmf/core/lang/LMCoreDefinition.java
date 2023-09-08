package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.*;
import isotropy.lmf.core.model.IModelPackage;
import isotropy.lmf.core.model.RawFeature;

import java.util.Arrays;
import java.util.List;

public interface LMCoreDefinition
{
	interface Features
	{
		interface LM_OBJECT
		{
			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of();
		}

		interface NAMED
		{
			Attribute<String, String> NAME = new AttributeImpl<>("name",
																 true,
																 false,
																 true,
																 Units.STRING,
																 List.of(),
																 Named.Features.name);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME);
		}

		interface TYPE
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME);
		}

		interface MODEL
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<String, String> DOMAIN = new AttributeImpl<>("domain",
																   true,
																   false,
																   true,
																   LMCoreDefinition.Units.STRING,
																   List.of(),
																   Model.Features.domain);

			Relation<Group<?>, List<Group<?>>> GROUPS = new RelationImpl<>("groups",
																		   true,
																		   true,
																		   false,
																		   new ReferenceImpl<>(() -> Groups.GROUP,
																							   List.of()),
																		   true,
																		   false,
																		   Model.Features.groups);

			Relation<Enum<?>, List<Enum<?>>> ENUMS = new RelationImpl<>("enums",
																		true,
																		true,
																		false,
																		new ReferenceImpl<>(() -> Groups.ENUM,
																							List.of()),
																		true,
																		false,
																		Model.Features.enums);

			Relation<Unit<?>, List<Unit<?>>> UNITS = new RelationImpl<>("units",
																		true,
																		true,
																		false,
																		new ReferenceImpl<>(() -> Groups.UNIT,
																							List.of()),
																		true,
																		false,
																		Model.Features.units);

			Relation<Alias, List<Alias>> ALIASES = new RelationImpl<>("aliases",
																	  true,
																	  true,
																	  false,
																	  new ReferenceImpl<>(() -> Groups.ALIAS,
																						  List.of()),
																	  true,
																	  false,
																	  Model.Features.aliases);

			Relation<JavaWrapper<?>, List<JavaWrapper<?>>> JAVA_WRAPPERS = new RelationImpl<>("javaWrappers",
																							  true,
																							  true,
																							  false,
																							  new ReferenceImpl<>(() -> Groups.JAVA_WRAPPER,
																												  List.of()),
																							  true,
																							  false,
																							  Model.Features.javaWrappers);

			Attribute<IModelPackage, IModelPackage> L_PACKAGE = new AttributeImpl<>("lPackage",
																					true,
																					false,
																					true,
																					JavaWrappers.I_MODEL_PACKAGE,
																					List.of(),
																					Model.Features.lPackage);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME,
																	 DOMAIN,
																	 GROUPS,
																	 ENUMS,
																	 UNITS,
																	 ALIASES,
																	 JAVA_WRAPPERS,
																	 L_PACKAGE);
		}

		interface GROUP
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<Boolean, Boolean> CONCRETE = new AttributeImpl<>("concrete",
																	   true,
																	   false,
																	   false,
																	   Units.BOOLEAN,
																	   List.of(),
																	   Group.Features.concrete);

			Relation<Reference<?>, List<Reference<?>>> INCLUDES = new RelationImpl<>("includes",
																					 true,
																					 true,
																					 false,
																					 new ReferenceImpl<>(() -> Groups.REFERENCE,
																										 List.of()),
																					 true,
																					 false,
																					 Group.Features.includes);

			Relation<Feature<?, ?>, List<Feature<?, ?>>> FEATURES = new RelationImpl<>("features",
																					   true,
																					   true,
																					   false,
																					   new ReferenceImpl<>(() -> Groups.FEATURE,
																										   List.of()),
																					   true,
																					   false,
																					   Group.Features.features);

			Relation<Generic<?>, List<Generic<?>>> GENERICS = new RelationImpl<>("generics",
																				 true,
																				 true,
																				 false,
																				 new ReferenceImpl<>(() -> Groups.GENERIC,
																									 List.of()),
																				 true,
																				 false,
																				 Group.Features.generics);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME,
																	 CONCRETE,
																	 INCLUDES,
																	 FEATURES,
																	 GENERICS																	 );
		}

		interface FEATURE
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<Boolean, Boolean> IMMUTABLE = new AttributeImpl<>("immutable",
																		true,
																		false,
																		false,
																		Units.BOOLEAN,
																		List.of(),
																		Feature.Features.immutable);

			Attribute<Boolean, Boolean> MANY = new AttributeImpl<>("many",
																   true,
																   false,
																   false,
																   Units.BOOLEAN,
																   List.of(),
																   Feature.Features.many);

			Attribute<Boolean, Boolean> MANDATORY = new AttributeImpl<>("mandatory",
																		true,
																		false,
																		false,
																		Units.BOOLEAN,
																		List.of(),
																		Feature.Features.mandatory);

			Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = new AttributeImpl<>("rawFeature",
																							true,
																							false,
																							false,
																							JavaWrappers.RAW_FEATURE,
																							List.of(),
																							Feature.Features.rawFeature);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME, IMMUTABLE, MANY, MANDATORY, RAW_FEATURE);
		}

		interface ATTRIBUTE
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<Boolean, Boolean> IMMUTABLE = LMCoreDefinition.Features.FEATURE.IMMUTABLE;

			Attribute<Boolean, Boolean> MANY = LMCoreDefinition.Features.FEATURE.MANY;

			Attribute<Boolean, Boolean> MANDATORY = LMCoreDefinition.Features.FEATURE.MANDATORY;

			Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = LMCoreDefinition.Features.FEATURE.RAW_FEATURE;

			Relation<Datatype<?>, Datatype<?>> DATATYPE = new RelationImpl<>("datatype",
																			 true,
																			 false,
																			 true,
																			 new ReferenceImpl<>(() -> Groups.DATATYPE,
																								 List.of(() -> (Concept<?>) LMCoreDefinition.Generics.ATTRIBUTE.get(
																										 0))),
																			 false,
																			 false,
																			 Attribute.Features.datatype);

			Relation<Generic<?>, List<Generic<?>>> PARAMETERS = new RelationImpl<>("parameters",
																				   true,
																				   true,
																				   false,
																				   new ReferenceImpl<>(() -> Groups.GENERIC,
																									   List.of()),
																				   false,
																				   false,
																				   Attribute.Features.parameters);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME,
																	 IMMUTABLE,
																	 MANY,
																	 MANDATORY,
																	 RAW_FEATURE,
																	 DATATYPE,
																	 PARAMETERS);
		}

		interface RELATION
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<Boolean, Boolean> IMMUTABLE = LMCoreDefinition.Features.FEATURE.IMMUTABLE;

			Attribute<Boolean, Boolean> MANY = LMCoreDefinition.Features.FEATURE.MANY;

			Attribute<Boolean, Boolean> MANDATORY = LMCoreDefinition.Features.FEATURE.MANDATORY;

			Attribute<RawFeature<?, ?>, RawFeature<?, ?>> RAW_FEATURE = LMCoreDefinition.Features.FEATURE.RAW_FEATURE;

			Relation<Reference<?>, Reference<?>> REFERENCE = new RelationImpl<>("reference",
																				true,
																				false,
																				true,
																				new ReferenceImpl<>(() -> Groups.REFERENCE,
																									List.of(() -> (Concept<?>) LMCoreDefinition.Generics.RELATION.get(
																											0))),
																				true,
																				false,
																				Relation.Features.reference);

			Attribute<Boolean, Boolean> LAZY = new AttributeImpl<>("lazy",
																   true,
																   false,
																   false,
																   Units.BOOLEAN,
																   List.of(),
																   Relation.Features.lazy);

			Attribute<Boolean, Boolean> CONTAINS = new AttributeImpl<>("contains",
																	   true,
																	   false,
																	   false,
																	   Units.BOOLEAN,
																	   List.of(),
																	   Relation.Features.contains);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME,
																	 IMMUTABLE,
																	 MANY,
																	 MANDATORY,
																	 RAW_FEATURE,
																	 REFERENCE,
																	 LAZY,
																	 CONTAINS);
		}

		interface DATATYPE
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME);
		}

		interface ALIAS
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<String, List<String>> WORDS = new AttributeImpl<>("words",
																		true,
																		true,
																		false,
																		Units.STRING,
																		List.of(),
																		Alias.Features.words);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME, WORDS);
		}

		interface ENUM
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<String, List<String>> LITERALS = new AttributeImpl<>("literals",
																		   true,
																		   true,
																		   false,
																		   Units.STRING,
																		   List.of(),
																		   Enum.Features.literals);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME, LITERALS);
		}

		interface UNIT
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<String, String> MATCHER = new AttributeImpl<>("matcher",
																	true,
																	false,
																	false,
																	Units.MATCHER,
																	List.of(),
																	Unit.Features.matcher);

			Attribute<String, String> DEFAULT_VALUE = new AttributeImpl<>("defaultValue",
																		  true,
																		  false,
																		  false,
																		  Units.STRING,
																		  List.of(),
																		  Unit.Features.defaultValue);

			Attribute<Primitive, Primitive> PRIMITIVE = new AttributeImpl<>("primitive",
																			true,
																			false,
																			true,
																			Enums.PRIMITIVE,
																			List.of(),
																			Unit.Features.primitive);

			Attribute<String, String> EXTRACTOR = new AttributeImpl<>("extractor",
																	  true,
																	  false,
																	  false,
																	  Units.EXTRACTOR,
																	  List.of(),
																	  Unit.Features.extractor);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME,
																	 MATCHER,
																	 DEFAULT_VALUE,
																	 PRIMITIVE,
																	 EXTRACTOR);
		}

		interface GENERIC
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Relation<Type<?>, Type<?>> TYPE = new RelationImpl<>("type",
																 true,
																 false,
																 false,
																 new ReferenceImpl<>(() -> Groups.TYPE,
																					 List.of(() -> (Concept<?>) LMCoreDefinition.Generics.GENERIC.get(
																							 0))),
																 false,
																 false,
																 Generic.Features.type);

			Attribute<BoundType, BoundType> BOUND_TYPE = new AttributeImpl<>("boundType",
																			 true,
																			 false,
																			 false,
																			 Enums.BOUND_TYPE,
																			 List.of(),
																			 Generic.Features.boundType);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME, TYPE, BOUND_TYPE);
		}

		interface REFERENCE
		{
			Relation<Concept<?>, Concept<?>> GROUP = new RelationImpl<>("group",
																		true,
																		false,
																		true,
																		new ReferenceImpl<>(() -> Groups.CONCEPT,
																							List.of(() -> LMCoreDefinition.Generics.REFERENCE.get(
																									0))),
																		false,
																		true,
																		Reference.Features.group);

			Relation<Concept<?>, List<Concept<?>>> PARAMETERS = new RelationImpl<>("parameters",
																				   true,
																				   true,
																				   false,
																				   new ReferenceImpl<>(() -> Groups.CONCEPT,
																									   List.of()),
																				   false,
																				   true,
																				   Reference.Features.parameters);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(GROUP, PARAMETERS);
		}

		interface CONCEPT
		{
			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of();
		}

		interface JAVA_WRAPPER
		{
			Attribute<String, String> NAME = LMCoreDefinition.Features.NAMED.NAME;

			Attribute<String, String> DOMAIN = new AttributeImpl<>("domain",
																   true,
																   false,
																   true,
																   Units.STRING,
																   List.of(),
																   JavaWrapper.Features.domain);

			List<isotropy.lmf.core.lang.Feature<?, ?>> ALL = List.of(NAME, DOMAIN);
		}
	}

	interface Generics
	{
		List<isotropy.lmf.core.lang.Generic<?>> TYPE = List.of(new GenericImpl<>("T", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> GROUP = List.of(new GenericImpl<>("T",
																				  isotropy.lmf.core.lang.BoundType.Extends,
																				  LMCoreDefinition.Groups.LM_OBJECT));

		List<isotropy.lmf.core.lang.Generic<?>> FEATURE = List.of(new GenericImpl<>("UnaryType", null, null),
																  new GenericImpl<>("EffectiveType", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> ATTRIBUTE = List.of(new GenericImpl<>("UnaryType", null, null),
																	new GenericImpl<>("EffectiveType", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> RELATION = List.of(new GenericImpl<>("UnaryType",
																					 isotropy.lmf.core.lang.BoundType.Extends,
																					 LMCoreDefinition.Groups.LM_OBJECT),
																   new GenericImpl<>("EffectiveType", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> DATATYPE = List.of(new GenericImpl<>("T", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> ENUM = List.of(new GenericImpl<>("T", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> UNIT = List.of(new GenericImpl<>("T", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> GENERIC = List.of(new GenericImpl<>("T",
																					BoundType.Extends,
																					LMCoreDefinition.Groups.LM_OBJECT));

		List<isotropy.lmf.core.lang.Generic<?>> REFERENCE = List.of(new GenericImpl<>("T",
																					  isotropy.lmf.core.lang.BoundType.Extends,
																					  LMCoreDefinition.Groups.LM_OBJECT));

		List<isotropy.lmf.core.lang.Generic<?>> CONCEPT = List.of(new GenericImpl<>("T", null, null));

		List<isotropy.lmf.core.lang.Generic<?>> JAVA_WRAPPER = List.of(new GenericImpl<>("T", null, null));
	}

	interface Groups
	{
		Group<LMObject> LM_OBJECT = new GroupImpl<>("LMObject",
																		   false,
																		   List.of(),
																		   Features.LM_OBJECT.ALL,
																		   List.of());

		Group<Named> NAMED = new GroupImpl<>("Named",
																	false,
																	List.of(new ReferenceImpl<>(() -> LM_OBJECT,
																								List.of())),
																	Features.NAMED.ALL,
																	List.of());

		isotropy.lmf.core.lang.Group<Type<?>> TYPE = new GroupImpl<>("Type",
																	 false,
																	 List.of(new ReferenceImpl<>(() -> NAMED,
																								 List.of())),
																	 Features.TYPE.ALL,
																	 Generics.TYPE);

		isotropy.lmf.core.lang.Group<Model> MODEL = new GroupImpl<>("Model",
																	true,
																	List.of(new ReferenceImpl<>(() -> NAMED,
																								List.of())),
																	Features.MODEL.ALL,
																	List.of());

		isotropy.lmf.core.lang.Group<Feature<?, ?>> FEATURE = new GroupImpl<>("Feature",
																			  false,
																			  List.of(new ReferenceImpl<>(() -> NAMED,
																										  List.of())),
																			  Features.FEATURE.ALL,
																			  Generics.FEATURE);

		isotropy.lmf.core.lang.Group<Attribute<?, ?>> ATTRIBUTE = new GroupImpl<>("Attribute",
																				  true,
																				  List.of(new ReferenceImpl<>(() -> FEATURE,
																											  List.of(() -> (Concept<?>) LMCoreDefinition.Generics.ATTRIBUTE.get(
																															  0),
																													  () ->(Concept<?>)  LMCoreDefinition.Generics.ATTRIBUTE.get(
																															  1)))),
																				  Features.ATTRIBUTE.ALL,
																				  Generics.ATTRIBUTE);

		isotropy.lmf.core.lang.Group<Relation<?, ?>> RELATION = new GroupImpl<>("Relation",
																				true,
																				List.of(new ReferenceImpl<>(() -> FEATURE,
																											List.of(() ->(Concept<?>)  LMCoreDefinition.Generics.RELATION.get(
																															0),
																													() ->(Concept<?>)  LMCoreDefinition.Generics.RELATION.get(
																															1)))),
																				Features.RELATION.ALL,
																				Generics.RELATION);

		isotropy.lmf.core.lang.Group<Datatype<?>> DATATYPE = new GroupImpl<>("Datatype",
																			 false,
																			 List.of(new ReferenceImpl<>(() -> TYPE,
																										 List.of(() ->(Concept<?>)  LMCoreDefinition.Generics.DATATYPE.get(
																												 0)))),
																			 Features.DATATYPE.ALL,
																			 Generics.DATATYPE);

		isotropy.lmf.core.lang.Group<Alias> ALIAS = new GroupImpl<>("Alias",
																	true,
																	List.of(new ReferenceImpl<>(() -> NAMED,
																								List.of())),
																	Features.ALIAS.ALL,
																	List.of());

		isotropy.lmf.core.lang.Group<Enum<?>> ENUM = new GroupImpl<>("Enum",
																	 true,
																	 List.of(new ReferenceImpl<>(() -> DATATYPE,
																								 List.of(() ->(Concept<?>)  LMCoreDefinition.Generics.ENUM.get(
																										 0)))),
																	 Features.ENUM.ALL,
																	 Generics.ENUM);

		Group<Unit<?>> UNIT = new GroupImpl<>("Unit",
											  true,
											  List.of(new ReferenceImpl<>(() -> DATATYPE,
																		  List.of(() -> (Concept<?>) LMCoreDefinition.Generics.UNIT.get(
																				  0)))),
											  Features.UNIT.ALL,
											  Generics.UNIT);

		Group<Reference<?>> REFERENCE = new GroupImpl<>("Reference",
														true,
														List.of(new ReferenceImpl<>(() -> LM_OBJECT, List.of())),
														Features.REFERENCE.ALL,
														Generics.REFERENCE);

		Group<Concept<?>> CONCEPT = new GroupImpl<>("Concept",
													false,
													List.of(),
													Features.CONCEPT.ALL,
													Generics.CONCEPT);

		isotropy.lmf.core.lang.Group<Group<?>> GROUP = new GroupImpl<>("Group",
																	   true,
																	   List.of(new ReferenceImpl<>(() -> TYPE,
																								   List.of(() ->(Concept<?>)  LMCoreDefinition.Generics.GROUP.get(
																										   0))),
																			   new ReferenceImpl<>(() -> CONCEPT,
																								   List.of(() ->(Concept<?>)  LMCoreDefinition.Generics.GROUP.get(
																										   0)))),
																	   Features.GROUP.ALL,
																	   Generics.GROUP);

		Group<Generic<?>> GENERIC = new GroupImpl<>("Generic",
													true,
													List.of(new ReferenceImpl<>(() -> CONCEPT,
																				List.of(() -> LMCoreDefinition.Generics.GENERIC.get(
																						0)))),
													Features.GENERIC.ALL,
													Generics.GENERIC);

		Group<JavaWrapper<?>> JAVA_WRAPPER = new GroupImpl<>("JavaWrapper",
															 true,
															 List.of(new ReferenceImpl<>(() -> DATATYPE,
																						 List.of(() -> (Concept<?>) LMCoreDefinition.Generics.JAVA_WRAPPER.get(
																								 0)))),
															 Features.JAVA_WRAPPER.ALL,
															 Generics.JAVA_WRAPPER);

		List<Group<?>> ALL = List.of(LM_OBJECT,
									 NAMED,
									 TYPE,
									 MODEL,
									 GROUP,
									 FEATURE,
									 ATTRIBUTE,
									 RELATION,
									 DATATYPE,
									 ALIAS,
									 ENUM,
									 UNIT,
									 GENERIC,
									 REFERENCE,
									 CONCEPT,
									 JAVA_WRAPPER);
	}

	interface Units
	{
		isotropy.lmf.core.lang.Unit<String> MATCHER = new UnitImpl<>("matcher",
																	 "rgx_match:<(.+?)>",
																	 null,
																	 Primitive.String,
																	 null);

		isotropy.lmf.core.lang.Unit<String> EXTRACTOR = new UnitImpl<>("extractor",
																	   "rgx_match:<(.+?)>",
																	   null,
																	   Primitive.String,
																	   null);

		isotropy.lmf.core.lang.Unit<Boolean> BOOLEAN = new UnitImpl<>("boolean",
																	  "rgx_match:<(true|false)>",
																	  "false",
																	  Primitive.Boolean,
																	  null);

		isotropy.lmf.core.lang.Unit<Integer> INT = new UnitImpl<>("int",
																  "rgx_match:<[0-9]+>",
																  "0",
																  Primitive.Int,
																  null);

		isotropy.lmf.core.lang.Unit<Long> LONG = new UnitImpl<>("long",
																"rgx_match:<[0-9]+[Ll]>",
																"0L",
																Primitive.Long,
																null);

		isotropy.lmf.core.lang.Unit<Float> FLOAT = new UnitImpl<>("float",
																  "rgx_match:<[0-9.]+[Ff]>",
																  "0f",
																  Primitive.Float,
																  null);

		isotropy.lmf.core.lang.Unit<Double> DOUBLE = new UnitImpl<>("double",
																	"rgx_match:<[0-9.]+>",
																	"0.",
																	Primitive.Double,
																	null);

		isotropy.lmf.core.lang.Unit<String> STRING = new UnitImpl<>("string", null, null, Primitive.String, null);

		List<isotropy.lmf.core.lang.Unit<?>> ALL = List.of(MATCHER,
														   EXTRACTOR,
														   BOOLEAN,
														   INT,
														   LONG,
														   FLOAT,
														   DOUBLE,
														   STRING);
	}

	interface Enums
	{
		isotropy.lmf.core.lang.Enum<BoundType> BOUND_TYPE = new EnumImpl<>("BoundType",
																		   Arrays.stream(BoundType.values())
																				 .map(java.lang.Enum::name)
																				 .toList());

		isotropy.lmf.core.lang.Enum<Primitive> PRIMITIVE = new EnumImpl<>("Primitive",
																		  Arrays.stream(Primitive.values())
																				.map(java.lang.Enum::name)
																				.toList());

		List<isotropy.lmf.core.lang.Enum<?>> ALL = List.of(BOUND_TYPE, PRIMITIVE);
	}

	interface Aliases
	{
		isotropy.lmf.core.lang.Alias DEFINITION = new AliasImpl("Definition", List.of("Group", "concrete"));

		isotropy.lmf.core.lang.Alias PLUS_CONTAINS = new AliasImpl("+contains",
																   List.of("Relation", "contains", "immutable=false"));

		isotropy.lmf.core.lang.Alias MINUS_CONTAINS = new AliasImpl("-contains",
																	List.of("Relation", "contains", "immutable"));

		isotropy.lmf.core.lang.Alias PLUS_REFERS = new AliasImpl("+refers",
																 List.of("Relation",
																		 "contains=false",
																		 "immutable=false"));

		isotropy.lmf.core.lang.Alias MINUS_REFERS = new AliasImpl("-refers",
																  List.of("Relation", "contains=false", "immutable"));

		isotropy.lmf.core.lang.Alias PLUS_ATT = new AliasImpl("+att", List.of("Attribute", "immutable=false"));

		isotropy.lmf.core.lang.Alias MINUS_ATT = new AliasImpl("-att", List.of("Attribute", "immutable"));

		isotropy.lmf.core.lang.Alias LSB_0_DOT_DOT_1_RSB = new AliasImpl("[0..1]",
																		 List.of("mandatory=false", "many=false"));

		isotropy.lmf.core.lang.Alias LSB_1_DOT_DOT_1_RSB = new AliasImpl("[1..1]", List.of("mandatory", "many=false"));

		isotropy.lmf.core.lang.Alias LSB_0_DOT_DOT_STAR_RSB = new AliasImpl("[0..*]",
																			List.of("mandatory=false", "many"));

		isotropy.lmf.core.lang.Alias LSB_1_DOT_DOT_STAR_RSB = new AliasImpl("[1..*]", List.of("mandatory", "many"));

		List<isotropy.lmf.core.lang.Alias> ALL = List.of(DEFINITION,
														 PLUS_CONTAINS,
														 MINUS_CONTAINS,
														 PLUS_REFERS,
														 MINUS_REFERS,
														 PLUS_ATT,
														 MINUS_ATT,
														 LSB_0_DOT_DOT_1_RSB,
														 LSB_1_DOT_DOT_1_RSB,
														 LSB_0_DOT_DOT_STAR_RSB,
														 LSB_1_DOT_DOT_STAR_RSB);
	}

	interface JavaWrappers
	{
		isotropy.lmf.core.lang.JavaWrapper<RawFeature<?, ?>> RAW_FEATURE = new JavaWrapperImpl<>("RawFeature",
																								 "isotropy.lmf.core.model");

		isotropy.lmf.core.lang.JavaWrapper<IModelPackage> I_MODEL_PACKAGE = new JavaWrapperImpl<>("IModelPackage",
																								  "isotropy.lmf.core.model");

		List<isotropy.lmf.core.lang.JavaWrapper<?>> ALL = List.of(RAW_FEATURE, I_MODEL_PACKAGE);
	}
}
