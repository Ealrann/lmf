package org.logoce.lmf.generator.model;

import org.logoce.lmf.core.lang.MetaModel;
import org.logoce.lmf.generator.util.TargetPathUtil;

import java.io.File;

public class ModelGenerator
{
	private final MetaModel model;

	public ModelGenerator(MetaModel model)
	{
		this.model = model;
	}

	public void generateJava(final File target)
	{
		final var resolvedTarget = TargetPathUtil.resolve(target, model);

		model.groups().stream().map(g -> new GroupGenerator(resolvedTarget, g)).forEach(GroupGenerator::generate);

		for (final var enumeration : model.enums())
		{
			final var enumGenerator = new EnumGenerator(enumeration);
			enumGenerator.generate(resolvedTarget);
		}

		final var modelDefinition = new ModelDefinition(model);
		modelDefinition.generate(resolvedTarget);

		final var modelPackage = new ModelPackage(model);
		modelPackage.generate(resolvedTarget);
	}
}
