package org.logoce.lmf.model.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.api.model.LilyBasicNotifier;
import org.logoce.lmf.model.api.notification.Notification;
import test.model.*;
import test.model.impl.PersonImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarCompanyModelRuntimeTest
{
	@Test
	void lmContainer_isSetAndUpdatedForContainments()
	{
		final var ceo1 = new PersonImpl("CEO 1");
		final var ceo2 = new PersonImpl("CEO 2");

		final var companyA = CarCompany.builder()
									   .name("Company A")
									   .ceo(() -> ceo1)
									   .build();
		final var companyB = CarCompany.builder()
									   .name("Company B")
									   .ceo(() -> ceo2)
									   .build();

		assertSame(companyA, ceo1.lmContainer(), "CEO 1 container should be company A");
		assertSame(companyB, ceo2.lmContainer(), "CEO 2 container should be company B");
		assertSame(CarCompanyDefinition.Features.CAR_COMPANY.CEO,
				   ceo1.lmContainingFeature(),
				   "CEO 1 containing feature should be CarCompany.ceo");
		assertSame(CarCompanyDefinition.Features.CAR_COMPANY.CEO,
				   ceo2.lmContainingFeature(),
				   "CEO 2 containing feature should be CarCompany.ceo");

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

		companyA.parcs().add(car1);
		companyA.parcs().add(car2);

		assertSame(companyA, car1.lmContainer(), "Car 1 container should be company A");
		assertSame(companyA, car2.lmContainer(), "Car 2 container should be company A");
		assertSame(CarCompanyDefinition.Features.CAR_COMPANY.PARCS,
				   car1.lmContainingFeature(),
				   "Car 1 containing feature should be CarCompany.parcs");
		assertSame(CarCompanyDefinition.Features.CAR_COMPANY.PARCS,
				   car2.lmContainingFeature(),
				   "Car 2 containing feature should be CarCompany.parcs");

		final var passenger1 = car1.passengers().getFirst();
		final var passenger2 = car2.passengers().getFirst();

		assertSame(car1, passenger1.lmContainer(), "Passenger 1 container should be car 1");
		assertSame(car2, passenger2.lmContainer(), "Passenger 2 container should be car 2");
		assertSame(CarCompanyDefinition.Features.CAR.PASSENGERS,
				   passenger1.lmContainingFeature(),
				   "Passenger 1 containing feature should be Car.passengers");
		assertSame(CarCompanyDefinition.Features.CAR.PASSENGERS,
				   passenger2.lmContainingFeature(),
				   "Passenger 2 containing feature should be Car.passengers");

		companyB.ceo(ceo1);

		assertSame(companyB, ceo1.lmContainer(), "CEO 1 container should move to company B");
		assertSame(CarCompanyDefinition.Features.CAR_COMPANY.CEO,
				   ceo1.lmContainingFeature(),
				   "CEO 1 containing feature should remain CarCompany.ceo");

		final var carParc = CarParc.builder().build();
		carParc.cars().add(car1);

		assertSame(carParc, car1.lmContainer(), "Car 1 container should move to car parc");
		assertSame(CarCompanyDefinition.Features.CAR_PARC.CARS,
				   car1.lmContainingFeature(),
				   "Car 1 containing feature should be CarParc.cars");

		car1.passengers().remove(passenger1);
		car2.passengers().add(passenger1);

		assertSame(car2, passenger1.lmContainer(), "Passenger 1 container should move to car 2");
		assertSame(CarCompanyDefinition.Features.CAR.PASSENGERS,
				   passenger1.lmContainingFeature(),
				   "Passenger 1 containing feature should remain Car.passengers");
	}

	@Test
	void listenAndSulk_shouldObserveAndUnsubscribeNotifications()
	{
		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> new PersonImpl("CEO"))
									  .build();

		final var car = Car.builder()
						   .name("Car")
						   .brand(Brand.Renault)
						   .build();

		((LilyBasicNotifier) company).eDeliver(true);
		((LilyBasicNotifier) car).eDeliver(true);

		final List<Notification> companyNotifications = new ArrayList<>();
		final List<Notification> carNotifications = new ArrayList<>();

		final Consumer<Notification> companyListener = companyNotifications::add;
		final Consumer<Notification> carListener = carNotifications::add;

		company.listen(companyListener, CarCompany.Features.parcs);
		car.listen(carListener, Car.Features.name);
		car.listen(carListener, Car.Features.passengers);

		car.name("Renamed Car");

		assertTrue(carNotifications.stream()
								   .anyMatch(n -> n.feature() == Car.Features.name &&
												  n.type() == Notification.EventType.SET &&
												  "Renamed Car".equals(n.newValue())),
				   "Renaming car should produce a SET notification on Car.name");

		company.parcs().add(car);

		assertTrue(companyNotifications.stream()
									   .anyMatch(n -> n.feature() == CarCompany.Features.parcs &&
													  n.type() == Notification.EventType.ADD &&
													  n.newValue() == car),
				   "Adding a car to parcs should produce an ADD notification on CarCompany.parcs");

		final var newPassenger = new PersonImpl("Passenger");
		car.passengers().add(newPassenger);

		assertTrue(carNotifications.stream()
								   .anyMatch(n -> n.feature() == Car.Features.passengers &&
												  n.type() == Notification.EventType.ADD &&
												  n.newValue() == newPassenger),
				   "Adding a passenger should produce an ADD notification on Car.passengers");

		final var previousCarNotificationCount = carNotifications.size();

		car.sulk(carListener, Car.Features.name);
		car.sulk(carListener, Car.Features.passengers);

		car.name("Renamed Again");
		car.passengers().add(new PersonImpl("Another Passenger"));

		assertEquals(previousCarNotificationCount,
					 carNotifications.size(),
					 "Notifications should stop after sulk is called");
	}
}

