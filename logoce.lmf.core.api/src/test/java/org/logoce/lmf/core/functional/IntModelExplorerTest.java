package org.logoce.lmf.core.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.util.IntModelExplorer;
import test.model.carcompany.*;
import test.model.carcompany.impl.PersonImpl;

import static org.junit.jupiter.api.Assertions.*;

public final class IntModelExplorerTest
{
	@Test
	void explore_singleLevelRelationOnCarParc_shouldReturnCars()
	{
		final var carParc = CarParc.builder()
								   .addCar(() -> Car.builder().name("Car 1").brand(Brand.Peugeot).build())
								   .addCar(() -> Car.builder().name("Car 2").brand(Brand.Renault).build())
								   .build();
		final var explorer = new IntModelExplorer(new int[]{CarParc.FeatureIDs.CARS});
		final var cars = explorer.explore(carParc, Car.class);

		assertEquals(2, cars.size());
		assertEquals("Car 1", cars.getFirst().name());
		assertEquals("Car 2", cars.get(1).name());
	}

	@Test
	void explore_multiLevelRelationOnCarCompany_shouldReturnPassengers()
	{
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> new PersonImpl("CEO"))
									  .addParc(() -> CarParc.builder()
															.addCar(() -> Car.builder()
																			 .name("Car 1")
																			 .brand(Brand.Peugeot)
																			 .addPassenger(() -> new PersonImpl(
																					 "Passenger 1"))
																			 .build())
															.build())
									  .addParc(() -> CarParc.builder()
															.addCar(() -> Car.builder()
																			 .name("Car 2")
																			 .brand(Brand.Renault)
																			 .addPassenger(() -> new PersonImpl(
																					 "Passenger 2"))
																			 .build())
															.build())
									  .build();
		final var explorer = new IntModelExplorer(new int[]{CarCompany.FeatureIDs.PARCS,
														   CarParc.FeatureIDs.CARS,
														   Car.FeatureIDs.PASSENGERS});
		final var passengers = explorer.explore(company, Person.class);

		assertEquals(2, passengers.size());
		final var names = passengers.stream().map(Person::name).toList();
		assertTrue(names.contains("Passenger 1"));
		assertTrue(names.contains("Passenger 2"));
	}

	@Test
	void explore_withParentClassOnPassenger_shouldClimbToCarAndCollectPassengers()
	{
		final var car = Car.builder()
						   .name("Car")
						   .brand(Brand.Peugeot)
						   .addPassenger(() -> new PersonImpl("Passenger 1"))
						   .addPassenger(() -> new PersonImpl("Passenger 2"))
						   .build();
		final var passenger = car.passengers().getFirst();
		final var explorer = new IntModelExplorer(Car.class, new int[]{Car.FeatureIDs.PASSENGERS});
		final var passengers = explorer.explore(passenger, Person.class);

		assertEquals(2, passengers.size());
		assertTrue(passengers.contains(passenger));
	}

	@Test
	void explore_withParentClassThreeLevelsOnPassenger_shouldReachCompanyCars()
	{
		final var car1 = Car.builder()
							.name("Car 1")
							.brand(Brand.Peugeot)
							.addPassenger(() -> new PersonImpl("Passenger 1"))
							.build();
		final var car2 = Car.builder()
							.name("Car 2")
							.brand(Brand.Renault)
							.addPassenger(() -> new PersonImpl("Passenger 2"))
							.build();
		final var parc1 = CarParc.builder().addCar(() -> car1).build();
		final var parc2 = CarParc.builder().addCar(() -> car2).build();
		CarCompany.builder()
				  .name("Company")
				  .ceo(() -> new PersonImpl("CEO"))
				  .addParc(() -> parc1)
				  .addParc(() -> parc2)
				  .build();
		final var passenger = car1.passengers().getFirst();
		final var explorer = new IntModelExplorer(CarCompany.class,
												  new int[]{CarCompany.FeatureIDs.PARCS,
															CarParc.FeatureIDs.CARS});
		final var cars = explorer.explore(passenger, Car.class);

		assertEquals(2, cars.size());
		final var names = cars.stream().map(Car::name).toList();
		assertTrue(names.contains("Car 1"));
		assertTrue(names.contains("Car 2"));
	}

	@Test
	void exploreAdaptAndExploreAdaptGeneric_shouldReturnCarAdaptersAlongPath()
	{
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> new PersonImpl("CEO"))
									  .addParc(() -> CarParc.builder()
															.addCar(() -> Car.builder()
																			 .name("Car 1")
																			 .brand(Brand.Peugeot)
																			 .build())
															.build())
									  .addParc(() -> CarParc.builder()
															.addCar(() -> Car.builder()
																			 .name("Car 2")
																			 .brand(Brand.Renault)
																			 .build())
															.build())
									  .build();
		final var explorer = new IntModelExplorer(new int[]{CarCompany.FeatureIDs.PARCS,
														   CarParc.FeatureIDs.CARS});
		final var adapters = explorer.exploreAdapt(company, ModelExplorerAdapters.CarAdapter.class);

		assertEquals(2, adapters.size());
		assertTrue(adapters.stream().map(ModelExplorerAdapters.CarAdapter::label).anyMatch("Car 1:Peugeot"::equals));
		assertTrue(adapters.stream().map(ModelExplorerAdapters.CarAdapter::label).anyMatch("Car 2:Renault"::equals));

		final var adaptersGeneric = explorer.exploreAdaptGeneric(company, ModelExplorerAdapters.CarAdapter.class);

		assertEquals(2, adaptersGeneric.size());
	}

	@Test
	void exploreAdaptNotNull_shouldReturnPersonAdapters()
	{
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> new PersonImpl("CEO"))
									  .addParc(() -> CarParc.builder()
															.addCar(() -> Car.builder()
																			 .name("Car 1")
																			 .brand(Brand.Peugeot)
																			 .addPassenger(() -> new PersonImpl(
																					 "Passenger 1"))
																			 .build())
															.build())
									  .build();
		final var explorer = new IntModelExplorer(new int[]{CarCompany.FeatureIDs.PARCS,
														   CarParc.FeatureIDs.CARS,
														   Car.FeatureIDs.PASSENGERS});
		final var adapters = explorer.exploreAdaptNotNull(company, ModelExplorerAdapters.PersonAdapter.class);

		assertEquals(1, adapters.size());
		final var adapter = adapters.getFirst();
		assertNotNull(adapter);
		assertEquals("PASSENGER 1", adapter.uppercaseName());
	}
}

