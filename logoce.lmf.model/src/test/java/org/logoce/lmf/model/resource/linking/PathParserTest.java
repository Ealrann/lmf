package org.logoce.lmf.model.resource.linking;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.loader.linking.feature.reference.PathParser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PathParserTest
{
	@Test
	public void localPath()
	{
		final var path = "../node1/node2";
		final var parser = new PathParser(path);

		final var step1 = parser.next();
		assertEquals(PathParser.Type.PARENT, step1.type());

		final var step2 = parser.next();
		assertEquals(PathParser.Type.CHILD, step2.type());
		assertEquals("node1", step2.text());

		final var step3 = parser.next();
		assertEquals(PathParser.Type.CHILD, step3.type());
		assertEquals("node2", step3.text());

		assertFalse(parser.hasNext());
	}

	@Test
	public void localPath2()
	{
		final var path = "./node1/node2";
		final var parser = new PathParser(path);

		final var step1 = parser.next();
		assertEquals(PathParser.Type.CURRENT, step1.type());

		final var step2 = parser.next();
		assertEquals(PathParser.Type.CHILD, step2.type());
		assertEquals("node1", step2.text());

		final var step3 = parser.next();
		assertEquals(PathParser.Type.CHILD, step3.type());
		assertEquals("node2", step3.text());

		assertFalse(parser.hasNext());
	}

	@Test
	public void localPath3()
	{
		final var path = "/node1/node2";
		final var parser = new PathParser(path);

		final var step1 = parser.next();
		assertEquals(PathParser.Type.ROOT, step1.type());

		final var step2 = parser.next();
		assertEquals(PathParser.Type.CHILD, step2.type());
		assertEquals("node1", step2.text());

		final var step3 = parser.next();
		assertEquals(PathParser.Type.CHILD, step3.type());
		assertEquals("node2", step3.text());

		assertFalse(parser.hasNext());
	}

	@Test
	public void modelPath()
	{
		final var path = "#Model1/node1/node2";
		final var parser = new PathParser(path);

		final var step1 = parser.next();
		assertEquals(PathParser.Type.MODEL, step1.type());
		assertEquals("Model1", step1.text());

		final var step2 = parser.next();
		assertEquals(PathParser.Type.CHILD, step2.type());
		assertEquals("node1", step2.text());

		final var step3 = parser.next();
		assertEquals(PathParser.Type.CHILD, step3.type());
		assertEquals("node2", step3.text());

		assertFalse(parser.hasNext());
	}

	@Test
	public void modelName()
	{
		final var path = "#Model1@toto";
		final var parser = new PathParser(path);

		final var step1 = parser.next();
		assertEquals(PathParser.Type.MODEL, step1.type());
		assertEquals("Model1", step1.text());

		final var step2 = parser.next();
		assertEquals(PathParser.Type.NAME, step2.type());
		assertEquals("toto", step2.text());

		assertFalse(parser.hasNext());
	}
}
