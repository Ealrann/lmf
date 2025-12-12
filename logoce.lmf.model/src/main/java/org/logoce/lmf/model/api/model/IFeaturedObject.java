package org.logoce.lmf.model.api.model;

import org.logoce.lmf.extender.api.IAdaptable;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.notification.api.IFeatures;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IFeaturedObject extends IAdaptable
{
	Group<?> lmGroup();
	LMObject lmContainer();
	int lmContainingFeatureID();
	Relation<?, ?, ?, ?> lmContainingFeature();

	<T> T get(Feature<?, ?, ?, ?> feature);
	<T> void set(Feature<?, ?, ?, ?> feature, T value);
	Object get(int featureID);
	void set(int featureID, Object object);

	default Stream<LMObject> lmStreamTree()
	{
		return ModelUtil.streamTree((LMObject) this);
	}

	int featureIndex(final int featureId);

	Stream<LMObject> streamChildren();
	Stream<LMObject> streamTree();

	@Override
	<T extends IAdapter> T adapt(Class<T> type);
	@Override
	<T extends IAdapter> T adapt(Class<T> type, String identifier);
	<T extends IAdapter> T adaptNotNull(Class<T> type);
	<T extends IAdapter> T adaptNotNull(Class<T> type, String identifier);

	<T extends IAdapter> T adaptGeneric(Class<? extends IAdapter> type);
	<T extends IAdapter> T adaptNotNullGeneric(Class<? extends IAdapter> type);

	IModelNotifier<? extends Features<?>> notifier();

	interface Features<T extends IFeatures<T>> extends IFeatures<T>
	{}

	interface Builder<T extends LMObject>
	{
		T build();

		<AttributeType> void push(Attribute<?, ?, ?, ?> feature, AttributeType value);

		<RelationType extends LMObject> void push(Relation<RelationType, ?, ?, ?> relation,
												  Supplier<RelationType> supplier);
	}
}
