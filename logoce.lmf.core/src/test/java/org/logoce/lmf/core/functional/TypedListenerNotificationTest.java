package org.logoce.lmf.core.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.lang.builder.RelationBuilder;
import org.logoce.lmf.core.api.notification.listener.Listener;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TypedListenerNotificationTest
{
	@Test
	void listenAndSulk_dispatchesTypedUnaryValues()
	{
		final var object = new DummyObject("a");

		final List<String> values = new ArrayList<>();
		final Listener<String> listener = (oldValue, newValue) -> values.add(oldValue + "->" + newValue);

		object.notifier().listen(listener, DummyObject.VALUE);
		object.value("b");

		assertEquals(List.of("a->b"), values);

		object.notifier().sulk(listener, DummyObject.VALUE);
		object.value("c");

		assertEquals(List.of("a->b"), values);
	}

	@Test
	void listen_dispatchesTypedManyValues_asLists()
	{
		final var object = new DummyObject("a");

		final List<List<String>> removed = new ArrayList<>();
		final List<List<String>> added = new ArrayList<>();
		final Listener<List<String>> listener = (oldValues, newValues) ->
		{
			removed.add(oldValues);
			added.add(newValues);
		};

		object.notifier().listen(listener, DummyObject.VALUES);

		object.values().add("x");
		object.values().addAll(List.of("y", "z"));
		object.values().remove("x");

		assertEquals(List.of(List.of(), List.of(), List.of("x")), removed);
		assertEquals(List.of(List.of("x"), List.of("y", "z"), List.of()), added);
	}

	@Test
	void listen_dispatchesTypedManyRelations_asLists()
	{
		final var object = new DummyObject("a");

		final List<List<LMObject>> removed = new ArrayList<>();
		final List<List<LMObject>> added = new ArrayList<>();
		final Listener<List<LMObject>> listener = (oldValues, newValues) ->
		{
			removed.add(oldValues);
			added.add(newValues);
		};

		object.notifier().listen(listener, DummyObject.CHILDREN);

		final var child = new DummyObject("child");
		object.children().add(child);
		object.children().remove(child);

		assertEquals(List.of(List.of(), List.of(child)), removed);
		assertEquals(List.of(List.of(child), List.of()), added);
	}

	@SuppressWarnings("unchecked")
	private static final class DummyObject extends FeaturedObject<LMObject.Features<?>> implements LMObject
	{
		static final int VALUE_ID = -100;
		static final int VALUES_ID = -101;
		static final int CHILDREN_ID = -102;

		static final Attribute<String, String, Listener<String>, LMObject.Features<?>> VALUE =
				new AttributeBuilder<String, String, Listener<String>, LMObject.Features<?>>()
						.name("value")
						.id(VALUE_ID)
						.datatype(() -> LMCoreModelDefinition.Units.STRING)
						.build();

		static final Attribute<String, List<String>, Listener<List<String>>, LMObject.Features<?>> VALUES =
				new AttributeBuilder<String, List<String>, Listener<List<String>>, LMObject.Features<?>>()
						.name("values")
						.id(VALUES_ID)
						.many(true)
						.datatype(() -> LMCoreModelDefinition.Units.STRING)
						.build();

		static final Relation<LMObject, List<LMObject>, Listener<List<LMObject>>, LMObject.Features<?>> CHILDREN =
				new RelationBuilder<LMObject, List<LMObject>, Listener<List<LMObject>>, LMObject.Features<?>>()
						.name("children")
						.id(CHILDREN_ID)
						.many(true)
						.contains(false)
						.concept(() -> LMCoreModelDefinition.Groups.LM_OBJECT)
						.build();

		private static final Group<LMObject> GROUP = Group.<LMObject>builder()
														  .name("Dummy")
														  .lmBuilder(new BuilderSupplier<>(() -> null))
														  .addFeature(() -> VALUE)
														  .addFeature(() -> VALUES)
														  .addFeature(() -> CHILDREN)
														  .build();

		private static final int FEATURE_COUNT = 3;
		private final ModelNotifier<LMObject.Features<?>> notifier = new ModelNotifier<>(this,
																						 FEATURE_COUNT,
																						 this::featureIndex);

		private String value;
		private final List<String> values = newObservableList(VALUES_ID, false, false);
		private final List<LMObject> children = newObservableList(CHILDREN_ID, true, false);

		private DummyObject(final String initial)
		{
			this.value = initial;
			notifier.eDeliver(true);
		}

		@Override
		public IModelNotifier.Impl<LMObject.Features<?>> notifier()
		{
			return notifier;
		}

		@Override
		public Group<LMObject> lmGroup()
		{
			return GROUP;
		}

		@Override
		public int featureIndex(final int featureId)
		{
			return switch (featureId)
			{
				case VALUE_ID -> 0;
				case VALUES_ID -> 1;
				case CHILDREN_ID -> 2;
				default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
			};
		}

		void value(final String newValue)
		{
			final var oldValue = value;
			value = newValue;
			notifier.notify(VALUE_ID, false, false, oldValue, newValue);
		}

		List<String> values()
		{
			return values;
		}

		List<LMObject> children()
		{
			return children;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T get(final Feature<?, ?, ?, ?> feature)
		{
			return switch (feature.id())
			{
				case VALUE_ID -> (T) value;
				case VALUES_ID -> (T) values;
				case CHILDREN_ID -> (T) children;
				default -> null;
			};
		}
	}
}
