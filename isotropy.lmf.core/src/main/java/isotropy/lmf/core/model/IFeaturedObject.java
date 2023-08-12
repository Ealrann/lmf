package isotropy.lmf.core.model;

import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
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

		<Type> void push(Feature<Type, ?> feature, Type value);
		<RelationType extends LMObject> void push(Relation<RelationType, ?> relation, Supplier<RelationType> supplier);
	}
}
