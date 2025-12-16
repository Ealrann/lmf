package org.logoce.lmf.core.loader.parsing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.lexer.ELMTokenType;
import org.logoce.lmf.core.api.loader.parsing.LmTreeReader;

public class LmTreeReaderTest
{
	private static final LmTreeReader treeReader = new LmTreeReader();

	@Test
	public void roots_singleElement()
	{
		final var model = "(model)";
		final var result = treeReader.read(model);

		final var roots = result.roots();
		Assertions.assertEquals(1, roots.size());

		final var root = roots.get(0);
		Assertions.assertEquals(1, root.data().tokens().size());
		final var firstToken = root.data().tokens().get(0);
		Assertions.assertEquals("model", firstToken.value());
		Assertions.assertEquals(ELMTokenType.TYPE, firstToken.type());
	}

	@Test
	public void roots_twoRoots()
	{
		final var model = "(model1)(model2)";
		final var result = treeReader.read(model);
		final var roots = result.roots();

		Assertions.assertEquals(2, roots.size());

		final var root1 = roots.get(0);
		Assertions.assertEquals("model1", root1.data().tokens().get(0).value());
		final var root2 = roots.get(1);
		Assertions.assertEquals("model2", root2.data().tokens().get(0).value());
	}

	@Test
	public void roots_depth3()
	{
		final var model = "(model (car (-int count) (-string name)))";
		final var result = treeReader.read(model);
		final var roots = result.roots();

		Assertions.assertEquals(1, roots.size());

		final var root = roots.get(0);
		Assertions.assertEquals("model", root.data().tokens().get(0).value());

		final var car = root.children().get(0);
		Assertions.assertEquals("car", car.data().tokens().get(0).value());

		final var count = car.children().get(0);
		final var countData = count.data();
		Assertions.assertEquals("-int", countData.tokens().get(0).value());
		Assertions.assertEquals("count", countData.tokens().get(2).value());

		final var name = car.children().get(1);
		final var nameData = name.data();
		Assertions.assertEquals("-string", nameData.tokens().get(0).value());
		Assertions.assertEquals("name", nameData.tokens().get(2).value());
	}

	@Test
	public void roots_matcher()
	{
		final var model = "(model (-matcher \"\\b(true|false)\\b\") (+int count) (-string name))";
		final var result = treeReader.read(model);
		final var roots = result.roots();

		final var root = roots.get(0);
		Assertions.assertEquals("model", root.data().tokens().get(0).value());

		final var matcher = root.children().get(0);
		Assertions.assertEquals("-matcher", matcher.data().tokens().get(0).value());
		Assertions.assertEquals("\\b(true|false)\\b", matcher.data().tokens().get(3).value());

		final var count = root.children().get(1);
		Assertions.assertEquals("+int", count.data().tokens().get(0).value());
		Assertions.assertEquals("count", count.data().tokens().get(2).value());

		final var name = root.children().get(2);
		Assertions.assertEquals("-string", name.data().tokens().get(0).value());
		Assertions.assertEquals("name", name.data().tokens().get(2).value());
	}
}

