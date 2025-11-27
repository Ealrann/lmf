package org.logoce.lmf.model.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.util.ModelExplorer;
import test.model.carcompany.*;
import test.model.carcompany.impl.PersonImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelExplorerTest
{
	@Test
	void explore_singleLevelRelationOnCarParc_shouldReturnCars()
	{
		final var carParc = CarParc.builder()
								   .addCar(() -> Car.builder()
													 .name("Car 1")
													 .brand(Brand.Peugeot)
													 .build())
								   .addCar(() -> Car.builder()
													 .name("Car 2")
													 .brand(Brand.Renault)
													 .build())
								   .build();
		final var explorer = new ModelExplorer(List.of(CarParc.Features.cars));
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
									  .addParc(() -> Car.builder()
														.name("Car 1")
														.brand(Brand.Peugeot)
														.addPassenger(() -> new PersonImpl("Passenger 1"))
														.build())
									  .addParc(() -> Car.builder()
														.name("Car 2")
														.brand(Brand.Renault)
														.addPassenger(() -> new PersonImpl("Passenger 2"))
														.build())
									  .build();
		final var explorer = new ModelExplorer(List.of(CarCompany.Features.parcs, Car.Features.passengers));
		final var passengers = explorer.explore(company, Person.class);

		assertEquals(2, passengers.size());
		final var names = passengers.stream().map(Person::name).toList();
		assertTrue(names.contains("Passenger 1"));
		assertTrue(names.contains("Passenger 2"));
	}

	@Test
	void explore_withParentHeightOnPassenger_shouldClimbToCarAndCollectPassengers()
	{
		final var car = Car.builder()
						   .name("Car")
						   .brand(Brand.Peugeot)
						   .addPassenger(() -> new PersonImpl("Passenger 1"))
						   .addPassenger(() -> new PersonImpl("Passenger 2"))
						   .build();
		final var passenger = car.passengers().getFirst();
		final var explorer = new ModelExplorer(1, List.of(Car.Features.passengers));
		final var passengers = explorer.explore(passenger, Person.class);

		assertEquals(2, passengers.size());
		assertTrue(passengers.contains(passenger));
	}

	@Test
	void explore_withParentHeightTwoLevelsOnPassenger_shouldReachCompanyCars()
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
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> new PersonImpl("CEO"))
									  .addParc(() -> car1)
									  .addParc(() -> car2)
									  .build();
		final var passenger = car1.passengers().getFirst();
		final var explorer = new ModelExplorer(2, List.of(CarCompany.Features.parcs));
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
									  .addParc(() -> Car.builder()
														.name("Car 1")
														.brand(Brand.Peugeot)
														.build())
									  .addParc(() -> Car.builder()
														.name("Car 2")
														.brand(Brand.Renault)
														.build())
									  .build();
		final var explorer = new ModelExplorer(List.of(CarCompany.Features.parcs));
		final var adapters = explorer.exploreAdapt(company, ModelExplorerAdapters.CarAdapter.class);

		assertEquals(2, adapters.size());
		assertTrue(adapters.stream().map(ModelExplorerAdapters.CarAdapter::label).anyMatch("Car 1:Peugeot"::equals));
		assertTrue(adapters.stream().map(ModelExplorerAdapters.CarAdapter::label).anyMatch("Car 2:Renault"::equals));

		final var adaptersGeneric = explorer.exploreAdaptGeneric(company,
																 ModelExplorerAdapters.CarAdapter.class);

		assertEquals(2, adaptersGeneric.size());
	}

	@Test
	void exploreAdaptNotNull_shouldReturnPersonAdapters()
	{
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> new PersonImpl("CEO"))
									  .addParc(() -> Car.builder()
														.name("Car 1")
														.brand(Brand.Peugeot)
														.addPassenger(() -> new PersonImpl("Passenger 1"))
														.build())
									  .build();
		final var explorer = new ModelExplorer(List.of(CarCompany.Features.parcs, Car.Features.passengers));
		final var adapters = explorer.exploreAdaptNotNull(company,
														  ModelExplorerAdapters.PersonAdapter.class);

		assertEquals(1, adapters.size());
		final var adapter = adapters.getFirst();
		assertNotNull(adapter);
		assertEquals("PASSENGER 1", adapter.uppercaseName());
	}
}
