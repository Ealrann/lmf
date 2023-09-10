package org.logoce.adapter.test;

import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.IAdapterProvider;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Adapters implements IAdapterProvider
{
	@Override
	public List<Class<? extends IAdapter>> classifiers()
	{
		return List.of(TestAdapter.TestAdapter1.class,
					   TestAdapter.TestAdapter2.class,
					   TestAdapter.TestSingletonAdapter.class,
					   TestAdapter.TestNamedAdapter1.class,
					   TestAdapter.TestNamedAdapter2.class,
					   TestAdapter.TestIdAdapter1.class,
					   TestAdapter.TestIdAdapter2.class);
	}

	@Override
	public MethodHandles.Lookup lookup()
	{
		return MethodHandles.lookup();
	}
}
