package isotropy.lmf.core.lang;

import isotropy.lmf.core.lang.builder.GroupReferenceBuilder;
import isotropy.lmf.core.lang.builder.RelationBuilder;
import isotropy.lmf.core.lang.impl.GenericImpl;

import java.util.List;
import java.util.function.Supplier;

public interface GroupReference<T extends LMObject> extends LMObject
{
	Group<T> group();

	Generic genericParameter();

	Group<?> directParameter();

	List<Generic> GENERICS = List.of(new GenericImpl("T", BoundType.Extends, LMCoreDefinition.Groups.LM_OBJECT));

	static <T extends LMObject> Builder<T> builder() {return new GroupReferenceBuilder<>();}
	interface Builder<UnaryType extends LMObject> extends LMObject.Builder<GroupReference<UnaryType>>
	{
		Builder<UnaryType> group(Supplier<Group<UnaryType>> group);
		Builder<UnaryType> genericParameter(Supplier<Generic> parameter);
		Builder<UnaryType> directParameter(Supplier<Group<?>> group);
	}
}
