package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AttributeBuilder;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;
import java.util.function.Supplier;

public interface Attribute<UnaryType, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Datatype<UnaryType> datatype();
	List<? extends Generic<?>> parameters();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<Boolean, Boolean> immutable = Feature.Features.immutable;
		RawFeature<Boolean, Boolean> many = Feature.Features.many;
		RawFeature<Boolean, Boolean> mandatory = Feature.Features.mandatory;
		RawFeature<Datatype<?>, Datatype<?>> datatype = new RawFeature<>(false,
																		 true,
																		 () -> LMCoreDefinition.Features.ATTRIBUTE.datatype);
		RawFeature<Generic<?>, List<? extends Generic<?>>> parameters = new RawFeature<>(true,
																						 true,
																						 () -> LMCoreDefinition.Features.ATTRIBUTE.parameters);
	}

	static <UnaryType, EffectiveType> Attribute.Builder<UnaryType, EffectiveType> builder() {return new AttributeBuilder<>();}
	interface Builder<UnaryType, EffectiveType> extends LMObject.Builder<Attribute<UnaryType, EffectiveType>>
	{
		Builder<UnaryType, EffectiveType> name(String name);
		Builder<UnaryType, EffectiveType> immutable(boolean immutable);
		Builder<UnaryType, EffectiveType> many(boolean many);
		Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);
		Builder<UnaryType, EffectiveType> rawFeature(RawFeature<UnaryType, EffectiveType> rawFeature);

		Builder<UnaryType, EffectiveType> datatype(Supplier<Datatype<UnaryType>> suppliedDatatype);
		Builder<UnaryType, EffectiveType> addParameter(Supplier<Generic<?>> parameter);
	}
}
