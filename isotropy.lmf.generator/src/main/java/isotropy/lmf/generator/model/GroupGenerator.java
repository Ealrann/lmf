package isotropy.lmf.generator.model;

import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.group.builder.BuilderGenerator;
import isotropy.lmf.generator.group.iface.InterfaceGenerator;
import isotropy.lmf.generator.group.impl.ImplementationGenerator;

import java.io.File;
import java.util.Optional;

public class GroupGenerator
{
	private final InterfaceGenerator interfaceGenerator;
	private final Optional<ImplementationGenerator> implementationGenerator;
	private final Optional<BuilderGenerator> builderGenerator;

	public GroupGenerator(final File targetDirectory, final Group<?> group)
	{
		final var isFinal = group.concrete();
		interfaceGenerator = new InterfaceGenerator(targetDirectory, group);
		implementationGenerator = isFinal ? Optional.of(new ImplementationGenerator(targetDirectory, group)) :
								  Optional.empty();
		builderGenerator = isFinal ? Optional.of(new BuilderGenerator(targetDirectory, group)) : Optional.empty();
	}

	public void generate()
	{
		interfaceGenerator.generate();

		implementationGenerator.ifPresent(ImplementationGenerator::generate);
		builderGenerator.ifPresent(BuilderGenerator::generate);
	}
}
