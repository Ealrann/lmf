package org.logoce.lmf.core.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.util.ModelCopier;
import test.model.carcompany.Brand;
import test.model.carcompany.Car;
import test.model.carcompany.CarCompany;
import test.model.carcompany.CarParc;
import test.model.carcompany.impl.PersonImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ModelCopierTest
{
	@Test
	void copyTree_shouldCloneContainmentsAndInternalReferences()
	{
		final var car = Car.builder()
						   .name("Car 1")
						   .brand(Brand.Peugeot)
						   .addPassenger(() -> new PersonImpl("Passenger 1"))
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

		final var copier = new ModelCopier();
		final var copiedCompany = copier.copyTree(company);

		assertNotSame(company, copiedCompany);
		assertEquals(company.name(), copiedCompany.name());

		final var originalCeo = company.ceo();
		final var copiedCeo = copiedCompany.ceo();
		assertNotSame(originalCeo, copiedCeo);
		assertSame(company, originalCeo.lmContainer());
		assertSame(copiedCompany, copiedCeo.lmContainer());

		final var originalParc = company.parcs().getFirst();
		final var copiedParc = copiedCompany.parcs().getFirst();

		final var originalCar = originalParc.cars().getFirst();
		final var copiedCar = copiedParc.cars().getFirst();
		assertNotSame(originalCar, copiedCar);
		assertEquals(originalCar.name(), copiedCar.name());
		assertEquals(originalCar.brand(), copiedCar.brand());
		assertSame(copiedCompany, copiedParc.lmContainer());
		assertSame(copiedParc, copiedCar.lmContainer());

		final var originalPassenger = originalCar.passengers().getFirst();
		final var copiedPassenger = copiedCar.passengers().getFirst();
		assertNotSame(originalPassenger, copiedPassenger);
		assertSame(originalCar, originalPassenger.lmContainer());
		assertSame(copiedCar, copiedPassenger.lmContainer());

		assertSame(originalCar, originalCeo.car());
		assertSame(copiedCar, copiedCeo.car());
	}

	@Test
	void copyTree_shouldKeepExternalReferences()
	{
		final var externalCar = Car.builder()
								   .name("External Car")
								   .brand(Brand.Renault)
								   .build();

		final var ceo = new PersonImpl("CEO");
		ceo.car(externalCar);

		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> ceo)
									  .build();

		final var copier = new ModelCopier();
		final var copiedCompany = copier.copyTree(company);

		final var copiedCeo = copiedCompany.ceo();
		assertNotSame(ceo, copiedCeo);
		assertSame(externalCar, copiedCeo.car());
	}
}
