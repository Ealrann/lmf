package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.generator.model.ModelGenerator;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Operation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OperationGenerationTest
{
	@Test
	public void generateOperationMethodWithContent() throws IOException
	{
		final var targetDir = new File("build/test-generated/operations");
		if (targetDir.exists())
		{
			deleteRecursively(targetDir);
		}

		final var metaModel = buildMetaModelWithOperation();
		final var generator = new ModelGenerator(metaModel);
		generator.generateJava(targetDir);

		final var basePackageDir = new File(targetDir, "test/operations");
		final var interfaceFile = new File(basePackageDir, "Foo.java");
		final var implFile = new File(new File(basePackageDir, "impl"), "FooImpl.java");

		assertTrue(interfaceFile.isFile(), "Foo.java should be generated");
		assertTrue(implFile.isFile(), "FooImpl.java should be generated");

		final var interfaceContent = Files.readString(interfaceFile.toPath(), StandardCharsets.UTF_8);
		final var implContent = Files.readString(implFile.toPath(), StandardCharsets.UTF_8);

		assertTrue(interfaceContent.contains("void ping()"), "Interface should declare ping()");
		assertTrue(implContent.contains("void ping()"), "Impl should implement ping()");
		assertTrue(implContent.contains("System.out.println(\"ping\");"), "Impl should contain operation body");
	}

	private static MetaModel buildMetaModelWithOperation()
	{
		final var operation = Operation.builder()
									   .name("ping")
									   .content("System.out.println(\"ping\");\n")
									   .build();

		@SuppressWarnings("rawtypes")
		final var groupBuilder = Group.builder();
		final Group<?> group = groupBuilder.name("Foo")
										   .concrete(true)
										   .addOperation(() -> operation)
										   .build();

		return MetaModel.builder()
						.name("OpsModel")
						.domain("test.operations")
						.addGroup(() -> group)
						.build();
	}

	private static void deleteRecursively(final File file)
	{
		if (file.isDirectory())
		{
			final var children = file.listFiles();
			if (children != null)
			{
				for (final var child : children)
				{
					deleteRecursively(child);
				}
			}
		}
		if (!file.delete() && file.exists())
		{
			throw new IllegalStateException("Cannot delete " + file);
		}
	}
}

