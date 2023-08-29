package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.*;
import isotropy.lmf.core.lang.impl.ModelImpl;
import isotropy.lmf.core.model.GroupDescriptor;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.model.IModelPackage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LMCorePackage implements IModelPackage
{
	public static final LMCorePackage Instance = new LMCorePackage();

	public static final List<GroupDescriptor<?>> groups = List.of(

			new GroupDescriptor<>(LMCoreDefinition.Groups.LM_OBJECT, null),
			new GroupDescriptor<>(LMCoreDefinition.Groups.NAMED, null),
			new GroupDescriptor<>(LMCoreDefinition.Groups.TYPE, null),
			new GroupDescriptor<>(LMCoreDefinition.Groups.CONCEPT, null),
			new GroupDescriptor<>(LMCoreDefinition.Groups.MODEL, ModelBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.GROUP, GroupBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.FEATURE, null),
			new GroupDescriptor<>(LMCoreDefinition.Groups.ATTRIBUTE, AttributeBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.RELATION, RelationBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.REFERENCE, ReferenceBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.DATATYPE, null),
			new GroupDescriptor<>(LMCoreDefinition.Groups.ALIAS, AliasBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.ENUM, EnumBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.UNIT, UnitBuilder::new),
			new GroupDescriptor<>(LMCoreDefinition.Groups.GENERIC, GenericBuilder::new));

	public static final List<Alias> aliases = List.of(LMCoreDefinition.Aliases.DEFINITION,
													  LMCoreDefinition.Aliases.PLUS_CONTAINS,
													  LMCoreDefinition.Aliases.MINUS_CONTAINS,
													  LMCoreDefinition.Aliases.PLUS_REFERS,
													  LMCoreDefinition.Aliases.MINUS_REFERS,
													  LMCoreDefinition.Aliases.PLUS_ATT,
													  LMCoreDefinition.Aliases.MINUS_ATT,
													  LMCoreDefinition.Aliases.LB_0_DOT_DOT_1_RB,
													  LMCoreDefinition.Aliases.LB_1_DOT_DOT_1_RB,
													  LMCoreDefinition.Aliases.LB_0_DOT_DOT_STAR_RB,
													  LMCoreDefinition.Aliases.LB_1_DOT_DOT_STAR_RB,
													  LMCoreDefinition.Aliases.DREF,
													  LMCoreDefinition.Aliases.GREF);

	public static final List<Unit<?>> units = List.of(LMCoreDefinition.Units.MATCHER,
													  LMCoreDefinition.Units.EXTRACTOR,
													  LMCoreDefinition.Units.BOOLEAN,
													  LMCoreDefinition.Units.INT,
													  LMCoreDefinition.Units.LONG,
													  LMCoreDefinition.Units.FLOAT,
													  LMCoreDefinition.Units.DOUBLE,
													  LMCoreDefinition.Units.STRING);
	public static final List<Enum<?>> enums = List.of(LMCoreDefinition.Enums.BOUND_TYPE,
													  LMCoreDefinition.Enums.PRIMITIVE);

	public static final Model MODEL = new ModelImpl(Instance,
													"LMCore",
													"isotrpoy.lmf.core.lang",
													groups.stream()
														  .map(GroupDescriptor::group)
														  .collect(Collectors.toUnmodifiableList()),
													enums,
													units,
													aliases);

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
												  .filter(groupDescriptor -> groupDescriptor.group() == group)
												  .findAny()
												  .orElseThrow()
												  .builder()
												  .get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> resolveEnum(final Enum<T> _enum, final String value)
	{
		if (_enum == LMCoreDefinition.Enums.BOUND_TYPE)
		{
			return (Optional<T>) Optional.of(BoundType.valueOf(value));
		}
		else if (_enum == LMCoreDefinition.Enums.PRIMITIVE)
		{
			return (Optional<T>) Optional.of(Primitive.valueOf(value));
		}

		return Optional.empty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<Class<T>> resolveClass(final Enum<T> _enum)
	{
		if (_enum == LMCoreDefinition.Enums.BOUND_TYPE)
		{
			return Optional.of((Class<T>) BoundType.class);
		}
		else if (_enum == LMCoreDefinition.Enums.PRIMITIVE)
		{
			return Optional.of((Class<T>) Primitive.class);
		}

		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends LMObject> Optional<Class<T>> resolveClass(final Group<T> group)
	{
		if (group == LMCoreDefinition.Groups.LM_OBJECT) return Optional.of((Class<T>) LMObject.class);
		else if (group == LMCoreDefinition.Groups.NAMED) return Optional.of((Class<T>) Named.class);
		else if (group == LMCoreDefinition.Groups.TYPE) return Optional.of((Class<T>) Type.class);
		else if (group == LMCoreDefinition.Groups.CONCEPT) return Optional.of((Class<T>) Concept.class);
		else if (group == LMCoreDefinition.Groups.MODEL) return Optional.of((Class<T>) Model.class);
		else if (group == LMCoreDefinition.Groups.GROUP) return Optional.of((Class<T>) Group.class);
		else if (group == LMCoreDefinition.Groups.FEATURE) return Optional.of((Class<T>) Feature.class);
		else if (group == LMCoreDefinition.Groups.ATTRIBUTE) return Optional.of((Class<T>) Attribute.class);
		else if (group == LMCoreDefinition.Groups.RELATION) return Optional.of((Class<T>) Relation.class);
		else if (group == LMCoreDefinition.Groups.DATATYPE) return Optional.of((Class<T>) Datatype.class);
		else if (group == LMCoreDefinition.Groups.ENUM) return Optional.of((Class<T>) Enum.class);
		else if (group == LMCoreDefinition.Groups.UNIT) return Optional.of((Class<T>) Unit.class);
		else if (group == LMCoreDefinition.Groups.GENERIC) return Optional.of((Class<T>) Generic.class);
		else if (group == LMCoreDefinition.Groups.REFERENCE) return Optional.of((Class<T>) Reference.class);

		return Optional.empty();
	}
}
