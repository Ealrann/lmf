package org.logoce.lmf.gradle;

import org.gradle.api.file.ConfigurableFileCollection;
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

	/**
	 * Extra `.lm` models available for resolving `imports=...` during generation, but not generated into the current
	 * project's output directory.
	 */
	public abstract ConfigurableFileCollection getImportModels();
}
