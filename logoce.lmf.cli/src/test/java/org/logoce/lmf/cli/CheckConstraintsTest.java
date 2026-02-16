package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.command.CheckCommand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CheckConstraintsTest
{
	@Test
	void checkConstraintsEmitsMandatoryWarningsButExitsOk(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("CarCompany.lm"), """
			(MetaModel domain=test.model name=CarCompany
				(Group Entity
					(includes group=#LMCore@Named))

				(Definition Person
					(includes group=@Entity))

				(Definition CarCompany
					(includes group=@Entity)
					(includes group=#LMCore@Model)
					(+contains ceo @Person [1..1])))
			""");

		Files.writeString(workspace.resolve("Instance.lm"), """
			(CarCompany domain=test.instance name=Instance metamodels=test.model.CarCompany)
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("Instance.lm", "--constraints"), context.err());
		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stderr = errBuffer.toString();
		assertTrue(stderr.contains("[WARNING]"), stderr);
		assertTrue(stderr.contains("Missing mandatory feature ceo ([1..1]) on CarCompany"), stderr);
	}

	@Test
	void checkConstraintsJsonIncludesWarningsButOk(@TempDir final Path workspace) throws Exception
	{
		Files.writeString(workspace.resolve("CarCompany.lm"), """
			(MetaModel domain=test.model name=CarCompany
				(Group Entity
					(includes group=#LMCore@Named))

				(Definition Person
					(includes group=@Entity))

				(Definition CarCompany
					(includes group=@Entity)
					(includes group=#LMCore@Model)
					(+contains ceo @Person [1..1])))
			""");

		Files.writeString(workspace.resolve("Instance.lm"), """
			(CarCompany domain=test.instance name=Instance metamodels=test.model.CarCompany)
			""");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var command = CheckCommand.parse(List.of("Instance.lm", "--constraints", "--json"), context.err());
		final int exit = command.execute(context);
		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer);

		final var stdout = outBuffer.toString().trim();
		JsonTestUtil.assertValidJson(stdout);
		assertTrue(stdout.contains("\"severity\":\"WARNING\""), stdout);
		assertTrue(stdout.contains("Missing mandatory feature ceo ([1..1]) on CarCompany"), stdout);
		assertTrue(stdout.contains("\"ok\":true"), stdout);
	}
}

