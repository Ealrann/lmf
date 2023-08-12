package isotropy.lm.core.resource.ptree.test;

import isotropy.lmf.core.resource.ptree.PTreeReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class PTreeReaderTest
{
	private static final PTreeReader treeBuilder = new PTreeReader();

	@Test
	public void testPTreeBuilder_singleElement()
	{
		final var model = "(model)";
		final var inputStream = new ByteArrayInputStream(model.getBytes());

		final var ptree = treeBuilder.read(inputStream);

		assertEquals(1, ptree.children().size());

		final var root = ptree.children().get(0);
		assertEquals(0, root.children().size());
		assertEquals(1, root.data().size());
		assertEquals("model", root.data().get(0));
	}

	@Test
	public void testPTreeBuilder_twoRoots()
	{
		final var model = "(model1)(model2)";
		final var inputStream = new ByteArrayInputStream(model.getBytes());

		final var ptree = treeBuilder.read(inputStream);

		assertEquals(2, ptree.children().size());

		final var root1 = ptree.children().get(0);
		assertEquals(0, root1.children().size());
		assertEquals(1, root1.data().size());
		assertEquals("model1", root1.data().get(0));
		final var root2 = ptree.children().get(1);
		assertEquals(0, root2.children().size());
		assertEquals(1, root2.data().size());
		assertEquals("model2", root2.data().get(0));
	}

	@Test
	public void testPTreeBuilder_depth3()
	{
		final var model = "(model (car (-int count) (-string name)))";
		final var inputStream = new ByteArrayInputStream(model.getBytes());

		final var ptree = treeBuilder.read(inputStream);

		assertEquals(1, ptree.children().size());

		final var root = ptree.children().get(0);
		assertEquals(1, root.children().size());
		assertEquals(1, root.data().size());
		assertEquals("model", root.data().get(0));

		final var car = root.children().get(0);
		assertEquals(2, car.children().size());
		assertEquals(1, car.data().size());
		assertEquals("car", car.data().get(0));

		final var count = car.children().get(0);
		assertEquals(0, count.children().size());
		assertEquals(2, count.data().size());
		assertEquals("-int", count.data().get(0));
		assertEquals("count", count.data().get(1));

		final var name = car.children().get(1);
		assertEquals(0, name.children().size());
		assertEquals(2, name.data().size());
		assertEquals("-string", name.data().get(0));
		assertEquals("name", name.data().get(1));
	}

	@Test
	public void testPTreeBuilder_matcher()
	{
		final var model = "(model (-matcher \"\\b(true|false)\\b\") (+int count) (-string name))";
		final var inputStream = new ByteArrayInputStream(model.getBytes());
		final var ptree = treeBuilder.read(inputStream);

		final var root = ptree.children().get(0);
		assertEquals(3, root.children().size());
		assertEquals(1, root.data().size());
		assertEquals("model", root.data().get(0));

		final var matcher = root.children().get(0);
		assertEquals(0, matcher.children().size());
		assertEquals(2, matcher.data().size());
		assertEquals("-matcher", matcher.data().get(0));
		assertEquals("\\b(true|false)\\b", matcher.data().get(1));

		final var count = root.children().get(1);
		assertEquals(0, count.children().size());
		assertEquals(2, count.data().size());
		assertEquals("+int", count.data().get(0));
		assertEquals("count", count.data().get(1));

		final var name = root.children().get(2);
		assertEquals(0, name.children().size());
		assertEquals(2, name.data().size());
		assertEquals("-string", name.data().get(0));
		assertEquals("name", name.data().get(1));
	}
}
