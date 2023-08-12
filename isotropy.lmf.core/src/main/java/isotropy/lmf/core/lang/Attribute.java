package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AttributeBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Attribute<UnaryType, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Datatype<UnaryType> datatype();

	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	static <UnaryType, EffectiveType> Attribute.Builder<UnaryType, EffectiveType> builder() {return new AttributeBuilder<>();}
	interface Builder<UnaryType, EffectiveType> extends LMObject.Builder<Attribute<UnaryType, EffectiveType>>
	{
		Builder<UnaryType, EffectiveType> name(String name);
		Builder<UnaryType, EffectiveType> immutable(boolean immutable);
		Builder<UnaryType, EffectiveType> many(boolean many);
		Builder<UnaryType, EffectiveType> mandatory(boolean mandatory);

		Builder<UnaryType, EffectiveType> datatype(Supplier<Datatype<UnaryType>> suppliedDatatype);
	}
}
