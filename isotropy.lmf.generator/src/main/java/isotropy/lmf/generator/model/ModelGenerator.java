package isotropy.lmf.generator.model;

import isotropy.lmf.core.lang.Model;

import java.io.File;

public class ModelGenerator
{
	private final Model model;

	public ModelGenerator(Model model)
	{
		this.model = model;
	}

	public void generateJava(final File target)
	{
		model.groups().stream().map(g -> new GroupGenerator(target, g)).forEach(GroupGenerator::generate);

		for (final var enumeration : model.enums())
		{
			final var enumGenerator = new EnumGenerator(enumeration);
			enumGenerator.generate(target);
		}

		final var modelDefinition = new ModelDefinition(model);
		modelDefinition.generate(target);

		final var modelPackage = new ModelPackage(model);
		modelPackage.generate(target);
	}
}
