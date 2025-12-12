package org.logoce.lmf.model.util;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.model.lang.LMCoreModelPackage;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class ModelRegistryTest
{
	@Test
	void register_throwsOnDuplicateQualifiedName()
	{
		final var builder = new ModelRegistry.Builder();
		builder.register(LMCoreModelPackage.MODEL);

		assertThrows(IllegalStateException.class, () -> builder.register(LMCoreModelPackage.MODEL));
	}
}

