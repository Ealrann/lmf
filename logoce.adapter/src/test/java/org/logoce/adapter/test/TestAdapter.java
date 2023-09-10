package org.logoce.adapter.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.logoce.adapter.api.Adapter;
import org.logoce.adapter.api.BasicAdaptable;
import org.logoce.extender.api.IAdaptable;
import org.logoce.extender.api.IAdapter;
import org.logoce.extender.api.ModelExtender;
import org.logoce.extender.ext.IAdaptableNameMatcher;

import static org.junit.jupiter.api.Assertions.*;

public class TestAdapter
{
	@BeforeEach
	public void load()
	{
		TestAdapter1.LOADED = false;
		TestAdapter2.LOADED = false;
	}

	@Test
	public void testAdapt()
	{
		final var src = new TestAdaptable("");

		assertFalse(TestAdapter1.LOADED);
		assertFalse(TestAdapter2.LOADED);

		final var adapter1 = src.adapt(TestAdapter1.class);

		assertNotNull(adapter1);
		assertTrue(TestAdapter1.LOADED);
		assertFalse(TestAdapter2.LOADED);

		final var adapter2 = src.adapt(TestAdapter2.class);

		assertNotNull(adapter1);
		assertNotNull(adapter2);
		assertTrue(TestAdapter1.LOADED);
		assertTrue(TestAdapter2.LOADED);
	}

	@Test
	public void testSingletonAdapt()
	{
		final var src1 = new TestAdaptable("");
		final var src2 = new TestAdaptable("");

		final var adapter1 = src1.adapt(TestSingletonAdapter.class);
		final var adapter2 = src2.adapt(TestSingletonAdapter.class);

		assertNotNull(adapter1);
		assertEquals(adapter1, adapter2);
	}

	@Test
	public void testName()
	{
		final var src1 = new TestAdaptable("1");
		final var src2 = new TestAdaptable("2");

		final var adapter1 = src1.adapt(INamedAdapter.class);
		final var adapter2 = src2.adapt(INamedAdapter.class);

		assertTrue(adapter1 instanceof TestNamedAdapter1);
		assertTrue(adapter2 instanceof TestNamedAdapter2);
	}

	@Test
	public void testId()
	{
		final var src = new TestAdaptable("");

		final var adapter1 = src.adapt(INamedAdapter.class, "1");
		final var adapter2 = src.adapt(INamedAdapter.class, "2");

		assertTrue(adapter1 instanceof TestIdAdapter1);
		assertTrue(adapter2 instanceof TestIdAdapter2);
	}

	public static final class TestAdaptable extends BasicAdaptable
	{
		private final String name;

		public TestAdaptable(final String name) {this.name = name;}
	}

	@ModelExtender(scope = TestAdaptable.class)
	@Adapter
	public static final class TestAdapter1 implements IAdapter
	{
		public static boolean LOADED = false;

		public TestAdapter1()
		{
			LOADED = true;
		}
	}

	@ModelExtender(scope = TestAdaptable.class)
	@Adapter
	public static final class TestAdapter2 implements IAdapter
	{
		public static boolean LOADED = false;

		public TestAdapter2()
		{
			LOADED = true;
		}
	}

	public interface INamedAdapter extends IAdapter {}

	@ModelExtender(scope = TestAdaptable.class, name = "1")
	@Adapter
	public static final class TestNamedAdapter1 implements INamedAdapter
	{}

	@ModelExtender(scope = TestAdaptable.class, name = "2")
	@Adapter
	public static final class TestNamedAdapter2 implements INamedAdapter
	{}

	@ModelExtender(scope = TestAdaptable.class, identifier = "1")
	@Adapter
	public static final class TestIdAdapter1 implements INamedAdapter
	{}

	@ModelExtender(scope = TestAdaptable.class, identifier = "2")
	@Adapter
	public static final class TestIdAdapter2 implements INamedAdapter
	{}

	@ModelExtender(scope = TestAdaptable.class)
	@Adapter(singleton = true)
	public static final class TestSingletonAdapter implements IAdapter
	{}

	public static final class TestNameMatcher implements IAdaptableNameMatcher
	{
		@Override
		public boolean match(final IAdaptable adaptable, final String name)
		{
			return adaptable instanceof TestAdaptable namedAdaptable && name.equals(namedAdaptable.name);
		}
	}
}
