package org.logoce.lmf.core.functional;

import org.logoce.lmf.adapter.api.Adapter;
import org.logoce.lmf.extender.api.IAdapter;
import org.logoce.lmf.extender.api.ModelExtender;
import test.model.carcompany.Car;
import test.model.carcompany.Person;

public final class ModelExplorerAdapters
{
	public interface CarAdapter extends IAdapter
	{
		String label();
	}

	@ModelExtender(scope = Car.class)
	@Adapter
	public static final class CarInfoAdapter implements CarAdapter
	{
		private final Car car;

		public CarInfoAdapter(final Car car)
		{
			this.car = car;
		}

		@Override
		public String label()
		{
			return car.name() + ":" + car.brand();
		}
	}

	public interface PersonAdapter extends IAdapter
	{
		String uppercaseName();
	}

	@ModelExtender(scope = Person.class)
	@Adapter
	public static final class PersonInfoAdapter implements PersonAdapter
	{
		private final Person person;

		public PersonInfoAdapter(final Person person)
		{
			this.person = person;
		}

		@Override
		public String uppercaseName()
		{
			return person.name().toUpperCase();
		}
	}
}
