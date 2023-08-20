package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.RelationBuilder;

import java.util.function.Supplier;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Reference<UnaryType> reference();
	boolean contains();

	static <UnaryType extends LMObject, EffectiveType> Builder<UnaryType, EffectiveType> builder() {return new RelationBuilder<UnaryType, EffectiveType>();}
	interface Builder<UnaryType extends LMObject, EffectiveType> extends LMObject.Builder<Relation<UnaryType, EffectiveType>>
	{
		Builder<UnaryType, EffectiveType> name(String name);
		Builder<UnaryType, EffectiveType> immutable(boolean immutable);
		Builder<UnaryType, EffectiveType> many(boolean many);
		Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
		Builder<UnaryType, EffectiveType> contains(boolean contains);
		Builder<UnaryType, EffectiveType> reference(Supplier<Reference<UnaryType>> groupReference);
	}
}
