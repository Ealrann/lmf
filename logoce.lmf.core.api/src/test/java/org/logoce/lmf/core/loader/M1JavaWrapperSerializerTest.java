package org.logoce.lmf.core.loader;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.LMCoreModelPackage;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import test.model.wrapperserialization.WrapperApp;
import test.model.wrapperserialization.WrapperSerializationModelDefinition;
import test.model.wrapperserialization.WrapperSerializationModelPackage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public final class M1JavaWrapperSerializerTest
{
	@Test
	void loadM1Model_resolvesJavaWrapperAttributeWithCompiledSerializer() throws IOException
	{
		final var converter = WrapperSerializationModelPackage.Instance.resolveJavaWrapperConverter(
				WrapperSerializationModelDefinition.JavaWrappers.DURATION);
		assertTrue(converter.isPresent(), "WrapperSerialization.Duration should provide a compiled converter");
		assertEquals(Duration.ofSeconds(20), converter.get().create("PT20S"));
		assertEquals("PT20S", converter.get().convert(Duration.ofSeconds(20)));

		final var registryBuilder = new ModelRegistry.Builder();
		registryBuilder.register(LMCoreModelPackage.MODEL);
		registryBuilder.register(WrapperSerializationModelPackage.MODEL);
		final var registry = registryBuilder.build();

		final var loader = new LmLoader(registry);
		final var source = Files.readString(Path.of("src/test/model/WrapperSerializationApp.lm"), StandardCharsets.UTF_8);
		final var objects = loader.loadObjects(source);

		assertEquals(1, objects.size(), "WrapperSerializationApp.lm should produce a single root object");
		assertInstanceOf(WrapperApp.class, objects.getFirst(), "Root object should be a WrapperApp instance");
		final var app = (WrapperApp) objects.getFirst();
		assertEquals(Duration.ofSeconds(20), app.duration());
	}
}

