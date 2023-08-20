package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Feature<UnaryType, EffectiveType> extends Named
{
	boolean immutable();
	boolean many();
	boolean mandatory();
	List<Generic<?>> GENERICS = List.of(new GenericImpl<>("UnaryType", null, null),
									 new GenericImpl<>("EffectiveType", null, null));

	Group<Feature<?, ?>> GROUP = LMCoreDefinition.Groups.FEATURE;

	interface Features extends LMCoreDefinition.Features.FEATURE {}
}
