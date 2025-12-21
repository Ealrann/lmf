package org.logoce.lmf.core.api.util;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.BuilderSupplier;
import org.logoce.lmf.core.api.model.FeaturedObject;
import org.logoce.lmf.core.api.model.IModelNotifier;
import org.logoce.lmf.core.api.model.ModelNotifier;
import org.logoce.lmf.core.api.notification.Notification;
import org.logoce.lmf.core.api.notification.listener.Listener;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.builder.RelationBuilder;
import test.model.carcompany.Brand;
import test.model.carcompany.Car;
import test.model.carcompany.CarCompany;
import test.model.carcompany.CarParc;
import test.model.carcompany.impl.PersonImpl;

import static org.junit.jupiter.api.Assertions.*;

final class ModelUtilDeleteTest
{
	@Test
	void delete_removesContainmentAndReferences()
	{
		final var car = Car.builder()
						   .name("Car 1")
						   .brand(Brand.Peugeot)
						   .build();
		final var ceo = new PersonImpl("CEO");
		ceo.car(car);

		final var parc = CarParc.builder()
								.addCar(() -> car)
								.build();
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> ceo)
									  .addParc(() -> parc)
									  .build();

		assertSame(company, ModelUtil.root(car), "Sanity: car root should be the company");
		assertSame(parc, car.lmContainer(), "Sanity: car container should be the parc");

		ModelUtil.delete(car);

		assertNull(ceo.car(), "delete() should clear non-containment references within the model");
		assertTrue(parc.cars().isEmpty(), "delete() should remove the object from containments within the model");
		assertNull(car.lmContainer(), "Deleted object should no longer have a container");
		assertNull(car.lmContainingFeature(), "Deleted object should no longer have a containing feature");
	}

	@Test
	void move_emitsSingleContainerChangeAndNeverNullContainer()
	{
		final var car = Car.builder()
						   .name("Car 1")
						   .brand(Brand.Peugeot)
						   .build();
		final var parcA = CarParc.builder().addCar(() -> car).build();
		final var parcB = CarParc.builder().build();
		CarCompany.builder()
				  .name("Company")
				  .ceo(() -> new PersonImpl("CEO"))
				  .addParc(() -> parcA)
				  .addParc(() -> parcB)
				  .build();

		assertSame(parcA, car.lmContainer(), "Sanity: car should be contained by parc A initially");

		final var containerChanges = new java.util.ArrayList<Notification>();
		car.notifier().listenStructure(notification ->
		{
			if (notification.type() == Notification.EventType.CONTAINER)
			{
				assertNotNull(car.lmContainer(), "lmContainer() must never be null during a move notification");
				containerChanges.add(notification);
			}
		});

		parcA.cars().remove(car);
		assertSame(parcA,
				   car.lmContainer(),
				   "During a move, lmContainer() should remain set until the object is attached to the new container");

		parcB.cars().add(car);

		assertSame(parcB, car.lmContainer(), "Car should be contained by parc B after the move");
		assertEquals(1, containerChanges.size(), "Move should emit a single CONTAINER notification");

		final var change = containerChanges.getFirst();
		assertSame(parcA, change.oldValue(), "CONTAINER notification should report the old container");
		assertSame(parcB, change.newValue(), "CONTAINER notification should report the new container");
	}

	@Test
	void delete_throwsWhenContainedInImmutableContainment()
	{
		final var child = new ImmutableChild();
		final var root = new ImmutableContainer(child);

		assertSame(root, child.lmContainer(), "Sanity: child should be contained by the root");
		assertTrue(child.lmContainingFeature().immutable(), "Sanity: containment relation should be immutable");

		assertThrows(IllegalStateException.class, () -> ModelUtil.delete(child));
	}

	private static final int IMMUTABLE_CHILD_ID = -2000;

	private static final Group<LMObject> IMMUTABLE_CHILD_GROUP = Group.<LMObject>builder()
																	  .name("ImmutableChild")
																	  .lmBuilder(new BuilderSupplier<>(() -> null))
																	  .build();

	private static final Relation<LMObject, LMObject, Listener<LMObject>, LMObject.Features<?>> IMMUTABLE_CHILD_RELATION =
			new RelationBuilder<LMObject, LMObject, Listener<LMObject>, LMObject.Features<?>>()
					.name("child")
					.id(IMMUTABLE_CHILD_ID)
					.contains(true)
					.immutable(true)
					.concept(() -> IMMUTABLE_CHILD_GROUP)
					.build();

	private static final Group<LMObject> IMMUTABLE_CONTAINER_GROUP = Group.<LMObject>builder()
																		  .name("ImmutableContainer")
																		  .lmBuilder(new BuilderSupplier<>(() -> null))
																		  .addFeature(() -> IMMUTABLE_CHILD_RELATION)
																		  .build();

	private static final class ImmutableContainer extends FeaturedObject<LMObject.Features<?>> implements LMObject
	{
		private static final int FEATURE_COUNT = 1;

		private final ModelNotifier<LMObject.Features<?>> notifier = new ModelNotifier<>(this,
																						 FEATURE_COUNT,
																						 this::featureIndex);
		private final LMObject child;

		private ImmutableContainer(final LMObject child)
		{
			this.child = child;
			setContainer(child, IMMUTABLE_CHILD_ID);
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
			return IMMUTABLE_CONTAINER_GROUP;
		}

		@Override
		public int featureIndex(final int featureId)
		{
			return switch (featureId)
			{
				case IMMUTABLE_CHILD_ID -> 0;
				default -> throw new IllegalArgumentException("Unknown featureId: " + featureId);
			};
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T get(final Feature<?, ?, ?, ?> feature)
		{
			return switch (feature.id())
			{
				case IMMUTABLE_CHILD_ID -> (T) child;
				default -> null;
			};
		}
	}

	private static final class ImmutableChild extends FeaturedObject<LMObject.Features<?>> implements LMObject
	{
		private static final int FEATURE_COUNT = 0;

		private final ModelNotifier<LMObject.Features<?>> notifier = new ModelNotifier<>(this,
																						 FEATURE_COUNT,
																						 this::featureIndex);

		private ImmutableChild()
		{
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
			return IMMUTABLE_CHILD_GROUP;
		}

		@Override
		public int featureIndex(final int featureId)
		{
			throw new IllegalArgumentException("Unknown featureId: " + featureId);
		}

		@Override
		public <T> T get(final Feature<?, ?, ?, ?> feature)
		{
			return null;
		}
	}
}
