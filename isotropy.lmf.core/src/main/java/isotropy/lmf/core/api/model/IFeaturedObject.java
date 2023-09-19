package isotropy.lmf.core.api.model;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.api.notification.Notification;
import isotropy.lmf.core.resource.util.ModelUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IFeaturedObject extends ILilyEObject
{
	Group<?> lmGroup();
	LMObject lmContainer();
	Relation<?, ?> lmContainingFeature();

	<T> T get(Feature<?, T> feature);
	<T> void set(Feature<?, T> feature, T value);

	default Stream<LMObject> lStream()
	{
		return ModelUtil.streamTree((LMObject) this);
	}

	void listenStruture(Consumer<Notification> listener);
	void sulkStructure(Consumer<Notification> listener);

	interface Builder<T extends LMObject>
	{
		T build();

		<AttributeType> void push(Attribute<AttributeType, ?> feature, AttributeType value);
		<RelationType extends LMObject> void push(Relation<RelationType, ?> relation, Supplier<RelationType> supplier);
	}
}
