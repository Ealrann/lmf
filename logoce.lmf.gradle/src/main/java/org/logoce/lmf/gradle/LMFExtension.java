package org.logoce.lmf.gradle;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;

import javax.inject.Inject;

public abstract class LMFExtension
{
	@Inject
	public LMFExtension(final ObjectFactory objects)
	{
	}

	public abstract DirectoryProperty getModelDir();

	public abstract DirectoryProperty getOutputDir();

	public abstract ListProperty<String> getIncludes();
}

