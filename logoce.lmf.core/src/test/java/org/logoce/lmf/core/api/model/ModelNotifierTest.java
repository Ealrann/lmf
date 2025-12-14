package org.logoce.lmf.core.api.model;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;
import org.logoce.lmf.core.notification.impl.IntSetNotification;
import org.logoce.lmf.core.notification.listener.IntListener;
import org.logoce.lmf.core.notification.listener.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class ModelNotifierTest
{
	@Test
	void notifyInt_dispatchesTypedAndNotificationListeners()
	{
		final var object = new DummyObject();
		object.notifier().eDeliver(true);

		final List<String> values = new ArrayList<>();
		final IntListener intListener = (oldValue, newValue) -> values.add(oldValue + "->" + newValue);
		object.notifier().listen(intListener, DummyObject.AGE);

		object.fastNotifier().notifyInt(DummyObject.AGE_ID, false, false, 1, 2);
		assertEquals(List.of("1->2"), values);

		final List<Object> notifications = new ArrayList<>();
		object.notifier().listen(notifications::add, DummyObject.AGE_ID);

		object.fastNotifier().notifyInt(DummyObject.AGE_ID, false, false, 2, 3);

		assertEquals(1, notifications.size());
		final var notification = assertInstanceOf(IntSetNotification.class, notifications.getFirst());
		assertEquals(DummyObject.AGE_ID, notification.featureId());
		assertEquals(3, notification.intValue());
	}

	@Test
	void notify_manyNormalizesValuesForListener()
	{
		final var object = new DummyObject();
		object.notifier().eDeliver(true);

		final List<List<String>> removed = new ArrayList<>();
		final List<List<String>> added = new ArrayList<>();

		final Listener<List<String>> listener = (oldValue, newValue) ->
		{
			removed.add(List.copyOf(oldValue));
			added.add(List.copyOf(newValue));
		};
		object.notifier().listen(listener, DummyObject.TAGS);

		object.fastNotifier().notify(DummyObject.TAGS_ID, false, true, null, "x");
		object.fastNotifier().notify(DummyObject.TAGS_ID, false, true, "x", null);

		assertEquals(List.of(List.of(), List.of("x")), removed);
		assertEquals(List.of(List.of("x"), List.of()), added);
	}

	@Test
	void notify_containmentTriggersStructureListenersEvenWhenFeatureIdIsUnknown()
	{
		final var object = new DummyObject();
		object.notifier().eDeliver(true);

		final var calls = new AtomicInteger();
		object.notifier().listenStructureNoParam(calls::incrementAndGet);

		object.fastNotifier().notify(-999, true, false, null, null);

		assertEquals(1, calls.get());
	}

	private static final class DummyObject extends FeaturedObject<LMObject.Features<?>> implements LMObject
	{
		static final int AGE_ID = -100;
		static final int TAGS_ID = -101;

		static final Attribute<Integer, Integer, IntListener, LMObject.Features<?>> AGE =
				new AttributeBuilder<Integer, Integer, IntListener, LMObject.Features<?>>()
						.name("age")
						.id(AGE_ID)
						.datatype(() -> LMCoreModelDefinition.Units.INT)
						.build();

		static final Attribute<String, List<String>, Listener<List<String>>, LMObject.Features<?>> TAGS =
				new AttributeBuilder<String, List<String>, Listener<List<String>>, LMObject.Features<?>>()
						.name("tags")
						.id(TAGS_ID)
						.many(true)
						.datatype(() -> LMCoreModelDefinition.Units.STRING)
						.build();

		private static final Group<LMObject> GROUP = Group.<LMObject>builder()
														  .name("Dummy")
														  .lmBuilder(new BuilderSupplier<>(() -> null))
														  .addFeature(() -> AGE)
														  .addFeature(() -> TAGS)
														  .build();

		private static final int FEATURE_COUNT = 2;
		private final ModelNotifier<LMObject.Features<?>> notifier = new ModelNotifier<>(this,
																						 FEATURE_COUNT,
																						 this::featureIndex);

		@Override
		public IModelNotifier.Impl<LMObject.Features<?>> notifier()
		{
			return notifier;
		}

		ModelNotifier<LMObject.Features<?>> fastNotifier()
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
				case AGE_ID -> 0;
				case TAGS_ID -> 1;
				default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
			};
		}

		@Override
		public <T> T get(final Feature<?, ?, ?, ?> feature)
		{
			return null;
		}
	}
}
