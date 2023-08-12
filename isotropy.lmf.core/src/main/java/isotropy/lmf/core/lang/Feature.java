package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;

public interface Feature<UnaryType, EffectiveType> extends Named
{
	boolean immutable();
	boolean many();
	boolean mandatory();
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));
}
