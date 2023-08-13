package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.*;
import isotropy.lmf.core.lang.impl.*;
import isotropy.lmf.core.model.GroupDescriptor;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.IModelPackage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LMCorePackage implements IModelPackage
{
	public static final LMCorePackage Instance = new LMCorePackage();

	public static final List<GroupDescriptor<?>> groups = List.of(

			new GroupDescriptor<>(Groups.LMOBJECT_GROUP, null),
			new GroupDescriptor<>(Groups.NAMED_GROUP, null),
			new GroupDescriptor<>(Groups.TYPE_GROUP, null),
			new GroupDescriptor<>(Groups.MODEL_GROUP, ModelBuilder::new),
			new GroupDescriptor<>(Groups.GROUP_GROUP, GroupBuilder::new),
			new GroupDescriptor<>(Groups.FEATURE_GROUP, null),
			new GroupDescriptor<>(Groups.ATTRIBUTE_GROUP, AttributeBuilder::new),
			new GroupDescriptor<>(Groups.RELATION_GROUP, RelationBuilder::new),
			new GroupDescriptor<>(Groups.DATATYPE_GROUP, null),
			new GroupDescriptor<>(Groups.ALIAS_GROUP, AliasBuilder::new),
			new GroupDescriptor<>(Groups.ENUM_GROUP, EnumBuilder::new),
			new GroupDescriptor<>(Groups.UNIT_GROUP, UnitBuilder::new),
			new GroupDescriptor<>(Groups.GENERIC_GROUP, GenericBuilder::new));

	public static final Alias DEFINITION_ALIAS = new AliasImpl("Definition", List.of("Group", "concrete"));
	public static final Alias PLUS_CONTAINS_ALIAS = new AliasImpl("+contains",
																  List.of("Relation", "contains", "immutable=false"));
	public static final Alias MINUS_CONTAINS_ALIAS = new AliasImpl("-contains",
																   List.of("Relation", "contains", "immutable"));
	public static final Alias PLUS_REFERS_ALIAS = new AliasImpl("+refers",
																List.of("Relation",
																		"contains=false",
																		"immutable=false"));
	public static final Alias MINUS_REFERS_ALIAS = new AliasImpl("-refers",
																 List.of("Relation", "contains=false", "immutable"));
	public static final Alias PLUS_ATT_ALIAS = new AliasImpl("+att", List.of("Attribute", "immutable=false"));
	public static final Alias MINUS_ATT_ALIAS = new AliasImpl("-att", List.of("Attribute", "immutable"));
	public static final Alias LB_0_DOT_DOT_1_RB_ALIAS = new AliasImpl("[0..1]",
																	  List.of("mandatory=false", "many=false"));
	public static final Alias LB_1_DOT_DOT_1_RB_ALIAS = new AliasImpl("[1..1]", List.of("mandatory", "many=false"));
	public static final Alias LB_0_DOT_DOT_STAR_RB_ALIAS = new AliasImpl("[0..*]", List.of("mandatory=false", "many"));
	public static final Alias LB_1_DOT_DOT_STAR_RB_ALIAS = new AliasImpl("[1..*]", List.of("mandatory", "many"));

	public static final List<Alias> aliases = List.of(DEFINITION_ALIAS,
													  PLUS_CONTAINS_ALIAS,
													  MINUS_CONTAINS_ALIAS,
													  PLUS_REFERS_ALIAS,
													  MINUS_REFERS_ALIAS,
													  PLUS_ATT_ALIAS,
													  MINUS_ATT_ALIAS,
													  LB_0_DOT_DOT_1_RB_ALIAS,
													  LB_1_DOT_DOT_1_RB_ALIAS,
													  LB_0_DOT_DOT_STAR_RB_ALIAS,
													  LB_1_DOT_DOT_STAR_RB_ALIAS);

	public static final List<Unit<?>> units = List.of(LMCoreTypes.MATCHER_UNIT,
													  LMCoreTypes.EXTRACTOR_UNIT,
													  LMCoreTypes.BOOLEAN_UNIT,
													  LMCoreTypes.INT_UNIT,
													  LMCoreTypes.LONG_UNIT,
													  LMCoreTypes.FLOAT_UNIT,
													  LMCoreTypes.DOUBLE_UNIT,
													  LMCoreTypes.STRING_UNIT);
	public static final List<Enum<?>> enums = List.of(LMCoreTypes.BOUND_TYPE_ENUM, LMCoreTypes.PRIMITIVE_ENUM);
	public static final Model MODEL = new ModelImpl(Instance,
													"lmcore",
													groups.stream()
														  .map(GroupDescriptor::group)
														  .collect(Collectors.toUnmodifiableList()),
													enums,
													units,
													aliases);

	static
	{
		groups.stream()
			  .map(GroupDescriptor::group)
			  .forEach(u -> u.lContainer(MODEL));
		enums.forEach(u -> u.lContainer(MODEL));
		units.forEach(u -> u.lContainer(MODEL));
		aliases.forEach(u -> u.lContainer(MODEL));
	}

	private LMCorePackage() {}

	@Override
	public Model model()
	{
		return MODEL;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends LMObject> IFeaturedObject.Builder<T> builder(final Group<T> group)
	{
		return (IFeaturedObject.Builder<T>) groups.stream()
												  .filter(groupDescriptor -> groupDescriptor.group == group)
												  .findAny()
												  .orElseThrow()
												  .builder()
												  .get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> resolveEnum(final Enum<T> _enum, final String value)
	{
		if (_enum == LMCoreTypes.BOUND_TYPE_ENUM)
		{
			return (Optional<T>) Optional.of(BoundType.valueOf(value));
		}
		else if (_enum == LMCoreTypes.PRIMITIVE_ENUM)
		{
			return (Optional<T>) Optional.of(Primitive.valueOf(value));
		}

		return Optional.empty();
	}

	public interface LMCoreTypes
	{
		Unit<String> MATCHER_UNIT = new UnitImpl<>("matcher",
												   "rgx_match:\\b(rgx_match:)*+\\b",
												   null,
												   Primitive.String,
												   null);
		Unit<String> EXTRACTOR_UNIT = new UnitImpl<>("extractor",
													 "rgx_match:\\b(rgx_value:)*+\\b",
													 null,
													 Primitive.String,
													 null);
		Unit<Boolean> BOOLEAN_UNIT = new UnitImpl<>("boolean", "\\b(true|false)\\b", "false", Primitive.Boolean, null);
		Unit<String> INT_UNIT = new UnitImpl<>("int", null, null, Primitive.Int, null);
		Unit<String> LONG_UNIT = new UnitImpl<>("long", null, null, Primitive.Long, null);
		Unit<String> FLOAT_UNIT = new UnitImpl<>("float", null, null, Primitive.Float, null);
		Unit<String> DOUBLE_UNIT = new UnitImpl<>("double", null, null, Primitive.Double, null);
		Unit<String> STRING_UNIT = new UnitImpl<>("string", null, null, Primitive.String, null);
		Enum<BoundType> BOUND_TYPE_ENUM = new EnumImpl<>("boundType",
														 Arrays.stream(BoundType.values())
															   .map(java.lang.Enum::name)
															   .toList());
		Enum<Primitive> PRIMITIVE_ENUM = new EnumImpl<>("primitive",
														Arrays.stream(Primitive.values())
															  .map(java.lang.Enum::name)
															  .toList());
	}

	public interface Groups
	{
		Group<LMObject> LMOBJECT_GROUP = new GroupImpl<>("LMObject", false, List.of(), List.of(), List.of());
		Group<Named> NAMED_GROUP = new GroupImpl<>("Named",
												   false,
												   List.of(LMOBJECT_GROUP),
												   LMCoreFeatures.Named_all,
												   List.of());
		Group<Type> TYPE_GROUP = new GroupImpl<>("Type", false, List.of(NAMED_GROUP), LMCoreFeatures.Type_all, List.of());
		Group<Model> MODEL_GROUP = new GroupImpl<>("Model",
												   true,
												   List.of(NAMED_GROUP),
												   LMCoreFeatures.Model_all,
												   List.of());
		Group<Group<?>> GROUP_GROUP = new GroupImpl<>("Group",
													  true,
													  List.of(TYPE_GROUP),
													  LMCoreFeatures.Group_all,
													  Group.GENERICS);
		Group<Feature<?, ?>> FEATURE_GROUP = new GroupImpl<>("Feature",
															 false,
															 List.of(NAMED_GROUP),
															 LMCoreFeatures.Feature_all,
															 Feature.GENERICS);
		Group<Attribute<?, ?>> ATTRIBUTE_GROUP = new GroupImpl<>("Attribute",
																 true,
																 List.of(FEATURE_GROUP),
																 LMCoreFeatures.Attribute_all,
																 Attribute.GENERICS);
		Group<Relation<?, ?>> RELATION_GROUP = new GroupImpl<>("Relation",
															   true,
															   List.of(FEATURE_GROUP),
															   LMCoreFeatures.Relation_all,
															   Relation.GENERICS);
		Group<Datatype<?>> DATATYPE_GROUP = new GroupImpl<>("Datatype",
															false,
															List.of(TYPE_GROUP),
															LMCoreFeatures.Datatype_all,
															Datatype.GENERICS);
		Group<Alias> ALIAS_GROUP = new GroupImpl<>("Alias",
												   true,
												   List.of(NAMED_GROUP),
												   LMCoreFeatures.Alias_all,
												   List.of());
		Group<Enum<?>> ENUM_GROUP = new GroupImpl<>("Enum",
													true,
													List.of(DATATYPE_GROUP),
													LMCoreFeatures.Enum_all,
													Enum.GENERICS);
		Group<Unit<?>> UNIT_GROUP = new GroupImpl<>("Unit",
													true,
													List.of(DATATYPE_GROUP),
													LMCoreFeatures.Unit_all,
													Unit.GENERICS);
		Group<Generic> GENERIC_GROUP = new GroupImpl<>("Generic",
													   true,
													   List.of(NAMED_GROUP),
													   LMCoreFeatures.Generic_all,
													   List.of());
	}
}
