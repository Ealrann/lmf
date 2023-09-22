package logoce.lmf.model.api.model;

import logoce.lmf.model.lang.Attribute;
import logoce.lmf.model.lang.Feature;
import logoce.lmf.model.lang.Group;
import logoce.lmf.model.lang.Relation;
import logoce.lmf.model.lang.LMObject;
import logoce.lmf.model.api.notification.Notification;
import logoce.lmf.model.resource.util.ModelUtil;

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
