package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.model.RawFeature;

import java.util.List;

public interface Feature<UnaryType, EffectiveType> extends Named
{
	boolean immutable();
	boolean many();
	boolean mandatory();
	RawFeature<UnaryType, EffectiveType> rawFeature();

	interface Features
	{
		RawFeature<String, String> name = Named.Features.name;
		RawFeature<Boolean, Boolean> immutable = new RawFeature<>(false,
																  false,
																  () -> LMCoreDefinition.Features.FEATURE.immutable);
		RawFeature<Boolean, Boolean> many = new RawFeature<>(false,
															 false,
															 () -> LMCoreDefinition.Features.FEATURE.many);
		RawFeature<Boolean, Boolean> mandatory = new RawFeature<>(false,
																  false,
																  () -> LMCoreDefinition.Features.FEATURE.mandatory);
	}

	List<Generic<?>> GENERICS = List.of(new GenericImpl<>("UnaryType", null, null),
										new GenericImpl<>("EffectiveType", null, null));
}
