package org.logoce.lmf.model.resource.ptree;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class PTreeReaderTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void testrootsBuilder_singleElement()
	{
		final var model = "(model)";
		final var inputStream = new ByteArrayInputStream(model.getBytes());

		final var roots = treeBuilder.read(inputStream);

		Assertions.assertEquals(1, roots.size());

		final var root = roots.get(0);
		Assertions.assertEquals(0, root.data().values().size());
		Assertions.assertEquals("model", root.data().type().firstToken());
	}

	@Test
	public void testrootsBuilder_twoRoots()
	{
		final var model = "(model1)(model2)";
		final var inputStream = new ByteArrayInputStream(model.getBytes());
		final var roots = treeBuilder.read(inputStream);

		Assertions.assertEquals(2, roots.size());

		final var root1 = roots.get(0);
		Assertions.assertEquals("model1", root1.data().type().firstToken());
		final var root2 = roots.get(1);
		Assertions.assertEquals("model2", root2.data().type().firstToken());
	}

	@Test
	public void testrootsBuilder_depth3()
	{
		final var model = "(model (car (-int count) (-string name)))";
		final var inputStream = new ByteArrayInputStream(model.getBytes());
		final var roots = treeBuilder.read(inputStream);

		Assertions.assertEquals(1, roots.size());

		final var root = roots.get(0);
		Assertions.assertEquals("model", root.data().type().firstToken());

		final var car = root.children().get(0);
		Assertions.assertEquals("car", car.data().type().firstToken());

		final var count = car.children().get(0);
		final var countData = count.data();
		Assertions.assertEquals("-int", countData.type().firstToken());
		Assertions.assertEquals("count", countData.values().get(0).firstToken());

		final var name = car.children().get(1);
		final var nameData = name.data();
		Assertions.assertEquals("-string", nameData.type().firstToken());
		Assertions.assertEquals("name", nameData.values().get(0).firstToken());
	}

	@Test
	public void testrootsBuilder_matcher()
	{
		final var model = "(model (-matcher \"\\b(true|false)\\b\") (+int count) (-string name))";
		final var inputStream = new ByteArrayInputStream(model.getBytes());
		final var roots = treeBuilder.read(inputStream);

		final var root = roots.get(0);
		Assertions.assertEquals("model", root.data().type().firstToken());

		final var matcher = root.children().get(0);
		Assertions.assertEquals("-matcher", matcher.data().type().firstToken());
		Assertions.assertEquals("\\b(true|false)\\b", matcher.data().values().get(0).firstToken());

		final var count = root.children().get(1);
		Assertions.assertEquals("+int", count.data().type().firstToken());
		Assertions.assertEquals("count", count.data().values().get(0).firstToken());

		final var name = root.children().get(2);
		Assertions.assertEquals("-string", name.data().type().firstToken());
		Assertions.assertEquals("name", name.data().values().get(0).firstToken());
	}
}
