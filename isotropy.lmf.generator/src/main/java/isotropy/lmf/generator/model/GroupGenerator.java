package isotropy.lmf.generator.model;

import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.group.builder.BuilderGenerator;
import isotropy.lmf.generator.group.impl.ImplementationGenerator;
import isotropy.lmf.generator.group.iface.InterfaceGenerator;

import java.util.Optional;

public class GroupGenerator
{
	private final InterfaceGenerator interfaceGenerator;
	private final Optional<ImplementationGenerator> implementationGenerator;
	private final Optional<BuilderGenerator> builderGenerator;

	public GroupGenerator(final GroupGenerationContext context)
	{
		final var isFinal = context.group()
								   .concrete();
		interfaceGenerator = new InterfaceGenerator(context);
		implementationGenerator = isFinal ? Optional.of(new ImplementationGenerator(context)) : Optional.empty();
		builderGenerator = isFinal ? Optional.of(new BuilderGenerator(context)) : Optional.empty();
	}

	public void generate()
	{
		interfaceGenerator.generate();

		implementationGenerator.ifPresent(ImplementationGenerator::generate);
		builderGenerator.ifPresent(BuilderGenerator::generate);
	}
}
