package org.logoce.lmf.model.util;

import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.IAdapterManager;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.api.notification.Notification;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Runtime-only {@link LMObject} implementation that stores values by {@link Feature} identity.
 * <p>
 * This intentionally avoids {@code feature.id()} / {@code featureIndex} and does not support
 * notifications or adapters. It is meant for "dynamic" meta-model scenarios where no generated
 * Java API exists for M1 objects.
 */
public final class DynamicFeaturedObject implements Model
{
	@SuppressWarnings("rawtypes")
	private static final IModelNotifier.Impl NOOP_NOTIFIER = new NoopModelNotifier();

	private final Group<?> group;
	private final Map<Feature<?, ?, ?, ?>, Object> values;
	private final List<Feature<?, ?, ?, ?>> allFeatures;

	private LMObject container;
	private Relation<?, ?, ?, ?> containingRelation;

	public DynamicFeaturedObject(final Group<?> group, final Map<Feature<?, ?, ?, ?>, Object> initialValues)
	{
		this.group = Objects.requireNonNull(group, "group");
		this.values = new IdentityHashMap<>();
		this.values.putAll(Objects.requireNonNull(initialValues, "initialValues"));
		this.allFeatures = collectAllFeatures(group);
	}

	@Override
	public Group<?> lmGroup()
	{
		return group;
	}

	@Override
	public LMObject lmContainer()
	{
		return container;
	}

	@Override
	public int lmContainingFeatureID()
	{
		return containingRelation == null ? -1 : containingRelation.id();
	}

	@Override
	public Relation<?, ?, ?, ?> lmContainingFeature()
	{
		return containingRelation;
	}

	@Override
	public <V> V get(final Feature<?, ?, ?, ?> feature)
	{
		if (feature == null) return null;

		if (feature.many())
		{
			@SuppressWarnings("unchecked")
			final V list = (V) values.computeIfAbsent(feature, ignored -> new ArrayList<>());
			return list;
		}

		@SuppressWarnings("unchecked")
		final V value = (V) values.get(feature);
		return value;
	}

	@Override
	public Object get(final int featureID)
	{
		return findFeatureById(featureID).map(this::get).orElseThrow(() ->
			new IllegalArgumentException("Unknown featureId " + featureID + " for group " + group.name()));
	}

	@Override
	public <V> void set(final Feature<?, ?, ?, ?> feature, final V value)
	{
		if (feature == null) return;
		values.put(feature, value);
	}

	@Override
	public void set(final int featureID, final Object object)
	{
		final var feature = findFeatureById(featureID).orElseThrow(() ->
			new IllegalArgumentException("Unknown featureId " + featureID + " for group " + group.name()));
		set(feature, object);
	}

	@Override
	public int featureIndex(final int featureId)
	{
		final var features = group.features();
		for (int i = 0; i < features.size(); i++)
		{
			if (features.get(i).id() == featureId)
			{
				return i;
			}
		}
		throw new IllegalArgumentException("Unknown featureId " + featureId + " for group " + group.name());
	}

	@Override
	public IModelNotifier.Impl<? extends Features<?>> notifier()
	{
		@SuppressWarnings({"rawtypes", "unchecked"})
		final IModelNotifier.Impl<? extends Features<?>> typed = (IModelNotifier.Impl) NOOP_NOTIFIER;
		return typed;
	}

	@Override
	public void listenStruture(final Consumer<Notification> listener)
	{
	}

	@Override
	public void sulkStructure(final Consumer<Notification> listener)
	{
	}

	@Override
	public Stream<LMObject> streamChildren()
	{
		return ModelUtil.streamContainmentFeatures(lmGroup()).flatMap(this::streamReference);
	}

	@Override
	public Stream<LMObject> streamTree()
	{
		return Stream.concat(Stream.of(this), streamChildren().flatMap(LMObject::streamTree));
	}

	@Override
	public <T extends IAdapter> T adapt(final Class<T> type)
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@Override
	public <T extends IAdapter> T adapt(final Class<T> type, final String identifier)
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@Override
	public <T extends IAdapter> T adaptNotNull(final Class<T> type)
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@Override
	public <T extends IAdapter> T adaptNotNull(final Class<T> type, final String identifier)
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@Override
	public <T extends IAdapter> T adaptGeneric(final Class<? extends IAdapter> type)
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@Override
	public <T extends IAdapter> T adaptNotNullGeneric(final Class<? extends IAdapter> type)
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@Override
	public String name()
	{
		final var nameAttribute = findAttribute("name");
		return nameAttribute == null ? null : (String) get(nameAttribute);
	}

	@Override
	public String domain()
	{
		final var domainAttribute = findAttribute("domain");
		return domainAttribute == null ? null : (String) get(domainAttribute);
	}

	@Override
	public List<String> imports()
	{
		final var importsAttribute = findAttribute("imports");
		if (importsAttribute == null) return List.of();

		final var value = get(importsAttribute);
		if (value == null) return List.of();
		if (value instanceof List<?> list)
		{
			@SuppressWarnings("unchecked")
			final var typed = (List<String>) list;
			return typed;
		}
		throw new IllegalStateException("Expected List value for 'imports' on group '" +
										group.name() +
										"', but got: " +
										value.getClass().getName());
	}

	@Override
	public List<String> metamodels()
	{
		final var metamodelsAttribute = findAttribute("metamodels");
		if (metamodelsAttribute == null) return List.of();

		final var value = get(metamodelsAttribute);
		if (value == null) return List.of();
		if (value instanceof List<?> list)
		{
			@SuppressWarnings("unchecked")
			final var typed = (List<String>) list;
			return typed;
		}
		throw new IllegalStateException("Expected List value for 'metamodels' on group '" +
										group.name() +
										"', but got: " +
										value.getClass().getName());
	}

	void attachContainment(final Relation<?, ?, ?, ?> relation, final LMObject child, final boolean many)
	{
		if (relation == null || child == null)
		{
			return;
		}

		if (many)
		{
			@SuppressWarnings("unchecked")
			final var list = (List<LMObject>) values.computeIfAbsent(relation, ignored -> new ArrayList<>());
			if (!list.contains(child))
			{
				list.add(child);
			}
		}
		else
		{
			values.put(relation, child);
		}

		if (relation.contains() && child instanceof DynamicFeaturedObject dynamicChild)
		{
			dynamicChild.container = this;
			dynamicChild.containingRelation = relation;
		}
	}

	private Optional<Feature<?, ?, ?, ?>> findFeatureById(final int featureId)
	{
		for (final var feature : allFeatures)
		{
			if (feature.id() == featureId)
			{
				return Optional.of(feature);
			}
		}
		return Optional.empty();
	}

	private Attribute<?, ?, ?, ?> findAttribute(final String attributeName)
	{
		if (attributeName == null) return null;

		for (final var feature : allFeatures)
		{
			if (feature instanceof Attribute<?, ?, ?, ?> attribute && attributeName.equals(attribute.name()))
			{
				return attribute;
			}
		}
		return null;
	}

	private static List<Feature<?, ?, ?, ?>> collectAllFeatures(final Group<?> group)
	{
		final var seen = Collections.newSetFromMap(new IdentityHashMap<Feature<?, ?, ?, ?>, Boolean>());
		return ModelUtil.streamAllFeatures(group).filter(seen::add).toList();
	}

	@SuppressWarnings("rawtypes")
	private static final class NoopModelNotifier implements IModelNotifier.Impl
	{
		private boolean deliver = true;

		@Override
		public void notify(final Notification notification)
		{
		}

		@Override
		public boolean eDeliver()
		{
			return deliver;
		}

		@Override
		public void eDeliver(final boolean deliver)
		{
			this.deliver = deliver;
		}

		@Override
		public void listen(final Consumer listener, final int... featureIDs)
		{
		}

		@Override
		public void sulk(final Consumer listener, final int... featureIDs)
		{
		}

		@Override
		public void listenNoParam(final Runnable listener, final int... featureIDs)
		{
		}

		@Override
		public void sulkNoParam(final Runnable listener, final int... featureIDs)
		{
		}

		@Override
		public void listen(final Consumer listener, final List features)
		{
		}

		@Override
		public void sulk(final Consumer listener, final List features)
		{
		}

		@Override
		public void listenNoParam(final Runnable listener, final List features)
		{
		}

		@Override
		public void sulkNoParam(final Runnable listener, final List features)
		{
		}

		@Override
		public void listenStructure(final Consumer listener)
		{
		}

		@Override
		public void sulkStructure(final Consumer listener)
		{
		}

		@Override
		public void listenStructureNoParam(final Runnable listener)
		{
		}

		@Override
		public void sulkStructureNoParam(final Runnable listener)
		{
		}
	}

	@Override
	public IAdapterManager adapterManager()
	{
		throw new UnsupportedOperationException("DynamicFeaturedObject does not support adapters");
	}

	@SuppressWarnings("unchecked")
	private Stream<LMObject> streamReference(final Relation<?, ?, ?, ?> relation)
	{
		if (relation.many())
		{
			final var value = get(relation);
			if (value == null) return Stream.empty();
			return ((List<LMObject>) value).stream();
		}

		return Stream.ofNullable(get(relation));
	}
}
