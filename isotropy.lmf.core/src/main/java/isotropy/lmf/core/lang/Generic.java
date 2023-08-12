package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GenericBuilder;
import isotropy.lmf.core.lang.impl.AttributeImpl;
import isotropy.lmf.core.lang.impl.RelationImpl;

import java.util.List;
import java.util.function.Supplier;

public interface Generic extends Named
{
	BoundType boundType();
	Type type();

	Group<Generic> GROUP = LMCorePackage.GENERIC_GROUP;

	interface Features
	{
		Attribute<String, String> name = Named.Features.name;
		Relation<Type, Type> type = new RelationImpl<>("type", true, false, true, Type.GROUP, false, null);
		Attribute<BoundType, BoundType> boundType = new AttributeImpl<>("boundType",
																		true,
																		false,
																		false,
																		LMCorePackage.BOUND_TYPE_ENUM);

		List<Feature<?, ?>> All = List.of(name, boundType);
	}

	static Builder builder() { return new GenericBuilder();}
	interface Builder extends LMObject.Builder<Generic>
	{
		Builder name(String name);
		Builder boundType(BoundType boundType);
		Builder type(Supplier<Type> type);
	}
}
