package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.resource.util.ModelUtil;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IFeaturedObject
{
	Group<?> lmGroup();
	LMObject lContainer();
	void lContainer(LMObject container);

	<T> T get(Feature<?, T> feature);
	<T> void set(Feature<?, T> feature, T value);

	default Stream<LMObject> lStream()
	{
		return ModelUtil.streamTree((LMObject) this);
	}

	interface Builder<T extends LMObject>
	{
		T build();

		<AttributeType> void push(Attribute<AttributeType, ?> feature, AttributeType value);
		<RelationType extends LMObject> void push(Relation<RelationType, ?> relation, Supplier<RelationType> supplier);
	}
}
