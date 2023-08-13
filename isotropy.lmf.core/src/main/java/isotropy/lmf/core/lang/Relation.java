package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.RelationBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Group<UnaryType> group();
	boolean contains();
	Generic parameter();

	List<Generic> GENERICS = List.of(new GenericImpl("T", BoundType.Extends, LMCoreDefinition.Groups.GROUP));

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
