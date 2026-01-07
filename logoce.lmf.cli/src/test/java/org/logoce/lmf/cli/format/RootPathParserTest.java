package org.logoce.lmf.cli.format;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RootPathParserTest
{
	@Test
	void parsesAtReferenceWithTraversalSteps()
	{
		final var parser = new RootPathParser("@Stone/..");
		final var steps = collect(parser);

		assertEquals(java.util.List.of(new RootPathParser.Step(RootPathParser.Type.NAME, "Stone"),
							new RootPathParser.Step(RootPathParser.Type.PARENT, "")),
				 steps);
	}

	@Test
	void parsesAtReferenceWithChildSteps()
	{
		final var parser = new RootPathParser("@ModelA/materials/materials.1");
		final var steps = collect(parser);

		assertEquals(java.util.List.of(new RootPathParser.Step(RootPathParser.Type.NAME, "ModelA"),
							new RootPathParser.Step(RootPathParser.Type.CHILD, "materials"),
							new RootPathParser.Step(RootPathParser.Type.CHILD, "materials.1")),
				 steps);
	}

	@Test
	void parsesAbsolutePathSteps()
	{
		final var parser = new RootPathParser("/materials/materials.1");
		final var steps = collect(parser);

		assertEquals(java.util.List.of(new RootPathParser.Step(RootPathParser.Type.ROOT, ""),
							new RootPathParser.Step(RootPathParser.Type.CHILD, "materials"),
							new RootPathParser.Step(RootPathParser.Type.CHILD, "materials.1")),
				 steps);
	}

	private static java.util.List<RootPathParser.Step> collect(final RootPathParser parser)
	{
		final var steps = new ArrayList<RootPathParser.Step>();
		while (parser.hasNext())
		{
			steps.add(parser.next());
		}
		return java.util.List.copyOf(steps);
	}
}
