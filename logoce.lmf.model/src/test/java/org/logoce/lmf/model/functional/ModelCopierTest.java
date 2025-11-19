package org.logoce.lmf.model.functional;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.util.ModelCopier;
import test.model.Brand;
import test.model.Car;
import test.model.CarCompany;
import test.model.Person;
import test.model.impl.PersonImpl;

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

		final var company = CarCompany.builder()
									  .name("Company")
									  .ceo(() -> ceo)
									  .addParc(() -> car)
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

		final var originalCar = company.parcs().getFirst();
		final var copiedCar = copiedCompany.parcs().getFirst();
		assertNotSame(originalCar, copiedCar);
		assertEquals(originalCar.name(), copiedCar.name());
		assertEquals(originalCar.brand(), copiedCar.brand());
		assertSame(copiedCompany, copiedCar.lmContainer());

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

