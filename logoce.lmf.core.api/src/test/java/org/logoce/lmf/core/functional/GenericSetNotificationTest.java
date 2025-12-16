package org.logoce.lmf.core.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.feature.FeatureSetter;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public final class GenericSetNotificationTest
{
	@Test
	void setByFeature_notifiesOnce_andDoesNotDoubleNotify()
	{
		final var object = new DummyObject("a");
		final List<Notification> notifications = new ArrayList<>();
		object.notifier().listen(notifications::add, DummyObject.VALUE_FEATURE_ID);

		object.set(DummyObject.VALUE_FEATURE, "b");

		assertEquals(1, notifications.size(), "Generic set(feature) should emit one notification");
		final var notification = notifications.getFirst();
		assertEquals(Notification.EventType.SET, notification.type(), "Event type should be SET");
		assertSame("b", notification.newValue(), "New value should be the assigned value");
		assertSame("a", notification.oldValue(), "Old value should be the previous value");
	}

	@Test
	void setById_notifiesOnce_andDoesNotDoubleNotify()
	{
		final var object = new DummyObject("a");
		final List<Notification> notifications = new ArrayList<>();
		object.notifier().listen(notifications::add, DummyObject.VALUE_FEATURE_ID);

		object.set(DummyObject.VALUE_FEATURE_ID, "b");

		assertEquals(1, notifications.size(), "Generic set(id) should emit one notification");
		final var notification = notifications.getFirst();
		assertEquals(Notification.EventType.SET, notification.type(), "Event type should be SET");
		assertSame("b", notification.newValue(), "New value should be the assigned value");
		assertSame("a", notification.oldValue(), "Old value should be the previous value");
	}

	private static final class DummyObject extends FeaturedObject<LMObject.Features<?>> implements LMObject
	{
		static final int VALUE_FEATURE_ID = -42;
		static final Feature<?, ?, ?, ?> VALUE_FEATURE = new AttributeBuilder<String, String, Object, LMObject.Features<?>>()
				.name("value")
				.id(VALUE_FEATURE_ID)
				.datatype(() -> LMCoreModelDefinition.Units.STRING)
				.build();
		private static final Group<LMObject> GROUP = Group.<LMObject>builder()
														  .name("Dummy")
														  .lmBuilder(new BuilderSupplier<>(() -> null))
														  .addFeature(() -> VALUE_FEATURE)
														  .build();

		private static final int FEATURE_COUNT = 1;
		private final ModelNotifier<LMObject.Features<?>> notifier = new ModelNotifier<>(this,
																						 FEATURE_COUNT,
																						 this::featureIndex);
		private String value;

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

		String value()
		{
			return value;
		}

		void value(final String newValue)
		{
			final var oldValue = this.value;
			this.value = newValue;
			notifier.notify(VALUE_FEATURE_ID, false, false, oldValue, newValue);
		}

		@Override
		public int featureIndex(final int featureId)
		{
			return featureIndexStatic(featureId);
		}

		static int featureIndexStatic(final int featureId)
		{
			if (featureId == VALUE_FEATURE_ID) return 0;
			throw new IllegalArgumentException("Unknown featureId: " + featureId);
		}

		@Override
		protected FeatureSetter<DummyObject> setterMap()
		{
			return Inserters.SET_MAP;
		}

		private static final class Inserters
		{
			private static final FeatureSetter<DummyObject> SET_MAP =
					new FeatureSetter.Builder<DummyObject>(FEATURE_COUNT, DummyObject::featureIndexStatic)
							.add(VALUE_FEATURE_ID, (object, newValue) -> object.value((String) newValue))
							.build();
		}
	}
}
