package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.AttributeBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;
import isotropy.lmf.core.lang.impl.RelationImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Attribute<UnaryType, EffectiveType> extends Feature<UnaryType, EffectiveType>
{
	Datatype<UnaryType> datatype();

	Group<Attribute<?, ?>> GROUP = LMCorePackage.ATTRIBUTE_GROUP;
	List<Generic> GENERICS = List.of(new GenericImpl("T", null, null));

	interface Features
	{
		Attribute<String, String> name = Named.Features.name;
		Attribute<Boolean, Boolean> immutable = Feature.Features.immutable;
		Attribute<Boolean, Boolean> many = Feature.Features.many;
		Attribute<Boolean, Boolean> mandatory = Feature.Features.mandatory;
		Relation<Datatype<?>, Datatype<?>> datatype = new RelationImpl<>("datatype",
																		 true,
																		 false,
																		 true,
																		 Datatype.GROUP,
																		 false,
																		 null);

		List<Feature<?, ?>> All = List.of(name, immutable, many, mandatory, datatype);
	}

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
