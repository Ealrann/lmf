package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AttributeBuilder;
import isotropy.lmf.core.lang.builder.RelationBuilder;
import isotropy.lmf.core.model.RawFeature;

import java.util.function.Supplier;

public interface Relation<UnaryType extends LMObject, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Reference<UnaryType> reference();
	boolean contains();
	boolean lazy();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<Boolean, Boolean> immutable = Feature.Features.immutable;
		RawFeature<Boolean, Boolean> many = Feature.Features.many;
		RawFeature<Boolean, Boolean> mandatory = Feature.Features.mandatory;
		RawFeature<Boolean, Boolean> contains = new RawFeature<>(false,
																 false,
																 () -> LMCoreDefinition.Features.RELATION.contains);
		RawFeature<Boolean, Boolean> lazy = new RawFeature<>(false,
																 false,
																 () -> LMCoreDefinition.Features.RELATION.lazy);
		RawFeature<Reference<?>, Reference<?>> reference = new RawFeature<>(false,
																			true,
																			() -> LMCoreDefinition.Features.RELATION.reference);
	}

	static <UnaryType extends LMObject, EffectiveType> Builder<UnaryType, EffectiveType> builder() {return new RelationBuilder<UnaryType, EffectiveType>();}
	interface Builder<UnaryType extends LMObject, EffectiveType> extends LMObject.Builder<Relation<UnaryType, EffectiveType>>
	{
		Builder<UnaryType, EffectiveType> name(String name);
		Builder<UnaryType, EffectiveType> immutable(boolean immutable);
		Builder<UnaryType, EffectiveType> many(boolean many);
		Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);

		Builder<UnaryType, EffectiveType> contains(boolean contains);
		Builder<UnaryType, EffectiveType> lazy(boolean lazy);
		Builder<UnaryType, EffectiveType> reference(Supplier<Reference<UnaryType>> groupReference);
		Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);
	}
}
