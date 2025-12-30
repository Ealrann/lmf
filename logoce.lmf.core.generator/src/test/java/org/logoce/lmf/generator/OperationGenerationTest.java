package org.logoce.lmf.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.core.lang.Operation;
import org.logoce.lmf.generator.model.ModelGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class OperationGenerationTest
{
	@Test
	public void generateOperationMethodWithContent(@TempDir final Path tempDir) throws IOException
	{
		final var metaModel = buildMetaModelWithOperation();
		final var targetDir = tempDir.resolve("operations").toFile();

		final var generator = new ModelGenerator(metaModel);
		generator.generateJava(targetDir);

		final var modelPackage = metaModel.name().toLowerCase(Locale.ROOT);
		final var basePackageDir = new File(targetDir, "test/operations/" + modelPackage);
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

	@Test
	public void generateOperationWithGenericParameters() throws IOException
	{
		final var basePackageDir = new File("src/test/generated/test/operations/generics/opsgeneric");
		final var interfaceFile = new File(basePackageDir, "Service.java");
		final var implFile = new File(new File(basePackageDir, "impl"), "ServiceImpl.java");

		assertTrue(interfaceFile.isFile(), "Service.java should be generated");
		assertTrue(implFile.isFile(), "ServiceImpl.java should be generated");

		final var interfaceContent = Files.readString(interfaceFile.toPath(), StandardCharsets.UTF_8);
		final var implContent = Files.readString(implFile.toPath(), StandardCharsets.UTF_8);

		assertTrue(interfaceContent.contains("interface Service<T> extends LMObject"),
				   "Interface should carry the group generic and extend LMObject");
		assertTrue(interfaceContent.contains("List<T> collect(List<T> items)"),
				   "Interface should declare parameterized operation signature");
		assertTrue(implContent.contains("List<T> collect(List<T> items)"),
				   "Implementation should implement the parameterized signature");
		assertTrue(implContent.contains("return items;"), "Implementation should keep the custom body");
	}

	private static MetaModel buildMetaModelWithOperation()
	{
		final var operation = Operation.builder()
									   .name("ping")
									   .content("System.out.println(\"ping\");\n")
									   .build();

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
}
