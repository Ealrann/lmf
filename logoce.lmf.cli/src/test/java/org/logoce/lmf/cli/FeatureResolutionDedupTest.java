package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.cli.assign.AddRunner;
import org.logoce.lmf.cli.assign.SetRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FeatureResolutionDedupTest
{
	@Test
	void addCanEditMetaModelImportsDespiteMultipleInheritance(@TempDir final Path workspace) throws Exception
	{
		writeGraphModels(workspace);
		final var file = workspace.resolve("GraphExtensions.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new AddRunner().run(context,
											 "GraphExtensions.lm",
											 "/",
											 "imports",
											 "test.multi.Dummy",
											 new AddRunner.Options(true));

		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer + "\nout:\n" + outBuffer);
		final var updated = Files.readString(file);
		assertTrue(updated.contains("imports=test.multi.GraphCore,test.multi.Dummy"), updated);
	}

	@Test
	void setCanEditInheritedNamedNameDespiteDiamondInheritance(@TempDir final Path workspace) throws Exception
	{
		writeCarCompanyModels(workspace);
		final var file = workspace.resolve("Peugeot.lm");

		final var outBuffer = new StringWriter();
		final var errBuffer = new StringWriter();
		final var context = new CliContext(workspace, new PrintWriter(outBuffer), new PrintWriter(errBuffer));

		final var exit = new SetRunner().run(context,
											 "Peugeot.lm",
											 "/",
											 "name",
											 "NewName",
											 new SetRunner.Options(true));

		assertEquals(ExitCodes.OK, exit, "err:\n" + errBuffer + "\nout:\n" + outBuffer);
		final var updated = Files.readString(file);
		assertTrue(updated.contains("CarCompany domain=test.model name=NewName"), updated);
	}

	private static void writeGraphModels(final Path workspace) throws Exception
	{
		final var graphCore = """
			(MetaModel domain=test.multi name=GraphCore
				(Group Node
					(+att name=name datatype=#LMCore@string [1..1]))
				(Enum Color Red,Green,Blue)
				(Definition ColoredNode
					(includes group=@Node)
					(+att name=color datatype=@Color [0..1])))
			""";

		final var graphExtensions = """
			(MetaModel domain=test.multi name=GraphExtensions imports=test.multi.GraphCore
				(Definition Graph
					(+contains name=nodes #GraphCore@Node [0..*])
					(+contains name=coloredNodes #GraphCore@ColoredNode [0..*])))
			""";

		final var dummy = """
			(MetaModel domain=test.multi name=Dummy
				(Group DummyGroup))
			""";

		Files.writeString(workspace.resolve("GraphCore.lm"), graphCore);
		Files.writeString(workspace.resolve("GraphExtensions.lm"), graphExtensions);
		Files.writeString(workspace.resolve("Dummy.lm"), dummy);
	}

	private static void writeCarCompanyModels(final Path workspace) throws Exception
	{
		final var meta = """
			(MetaModel domain=test.model name=CarCompany
				(Group Entity
					(includes group=#LMCore@Named))
				(Enum Brand Renault,Peugeot)
				(Definition Car
					(includes group=@Entity)
					(-att name=brand datatype=@Brand [1..1] defaultValue="Peugeot")
					(+contains name=passengers @Person [0..*])
					(+att age datatype=#LMCore@int)
					(+att weight datatype=#LMCore@float))
				(Definition CarParc
					(+contains cars [0..*] @Car))
				(Definition Person
					(includes group=@Entity)
					(+refers car [0..1] @Car))
				(Definition CarCompany
					(includes group=@Entity)
					(includes group=#LMCore@Model)
					(+contains ceo @Person [1..1])
					(+contains name=parcs @CarParc [0..*])))
			""";

		final var peugeot = """
			(CarCompany domain=test.model name=PeugeotCompany metamodels=test.model.CarCompany
				(ceo name=Macron)
				(CarParc
					(Car name=peugeot1 brand=Peugeot)))
			""";

		Files.writeString(workspace.resolve("CarCompany.lm"), meta);
		Files.writeString(workspace.resolve("Peugeot.lm"), peugeot);
	}
}

