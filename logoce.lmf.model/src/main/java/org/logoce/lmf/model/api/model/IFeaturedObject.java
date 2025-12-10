package org.logoce.lmf.model.api.model;

import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.util.ModelUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IFeaturedObject extends ILilyEObject
{
	Group<?> lmGroup();
	LMObject lmContainer();
	int lmContainingFeatureID();
	Relation<?, ?> lmContainingFeature();

	<T> T get(Feature<?, T> feature);
	<T> void set(Feature<?, T> feature, T value);

	Object get(int featureID);
	void set(int featureID, Object object);

	default Stream<LMObject> lmStreamTree()
	{
		return ModelUtil.streamTree((LMObject) this);
	}

	void listenStruture(Consumer<Notification> listener);
	void sulkStructure(Consumer<Notification> listener);

	int featureIndex(final int featureId);

	interface Builder<T extends LMObject>
	{
		T build();

		<AttributeType> void push(Attribute<AttributeType, ?> feature, AttributeType value);
		<RelationType extends LMObject> void push(Relation<RelationType, ?> relation, Supplier<RelationType> supplier);
	}
}
