package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.RelationBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.lang.impl.RelationImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Group<UnaryType> group();
	boolean contains();
	Generic parameter();

	Group<Relation<?, ?>> GROUP = LMCorePackage.RELATION_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", BoundType.Extends, Group.GROUP));

	interface Features
	{
		Attribute<String, String> name = Feature.Features.name;
		Attribute<Boolean, Boolean> immutable = Feature.Features.immutable;
		Attribute<Boolean, Boolean> many = Feature.Features.many;
		Attribute<Boolean, Boolean> mandatory = Feature.Features.mandatory;
		Relation<Group<?>, Group<?>> group = new RelationImpl<>("group", true, false, true, Group.GROUP, false, null);
		Attribute<Boolean, Boolean> contains = new AttributeImpl<>("contains",
																   true,
																   false,
																   false,
																   LMCorePackage.BOOLEAN_UNIT);
		Relation<Generic, Generic> parameter = new RelationImpl<>("parameter",
																  true,
																  false,
																  false,
																  Generic.GROUP,
																  false,
																  null);

		List<Feature<?, ?>> All = List.of(name, immutable, many, mandatory, group, contains, parameter);
	}

	static <UnaryType extends LMObject, EffectiveType> Builder<UnaryType, EffectiveType> builder() {return new RelationBuilder<UnaryType, EffectiveType>();}
	interface Builder<UnaryType extends LMObject, EffectiveType> extends LMObject.Builder<Relation<UnaryType, EffectiveType>>
	{
		Builder<UnaryType, EffectiveType> name(String name);
		Builder<UnaryType, EffectiveType> immutable(boolean immutable);
		Builder<UnaryType, EffectiveType> many(boolean many);
		Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
		Builder<UnaryType, EffectiveType> contains(boolean contains);

		Builder<UnaryType, EffectiveType> group(Supplier<Group<UnaryType>> group);
		Builder<UnaryType, EffectiveType> parameter(Supplier<Generic> parameter);
	}
}
