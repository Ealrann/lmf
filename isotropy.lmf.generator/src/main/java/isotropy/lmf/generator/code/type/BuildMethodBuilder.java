package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.ConstantTypes;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.GroupType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public class BuildMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	private final TypeName buildType;
	private final GroupType interfaceType;

	public BuildMethodBuilder(final GroupType interfaceType)
	{
		this.interfaceType = interfaceType;
		this.buildType = interfaceType.implementation().raw();
	}

	@Override
	public MethodSpec build(final List<FeatureResolution> context)
	{
		final var spec = MethodSpec.methodBuilder("build")
								   .addModifiers(Modifier.PUBLIC)
								   .returns(interfaceType.parametrized())
								   .addAnnotation(Override.class);
		final var arguments = context.stream().map(BuildArgument::of).toList();

		arguments.stream()
				 .map(BuildArgument::preOperation)
				 .filter(Optional::isPresent)
				 .map(Optional::get)
				 .forEach(spec::addStatement);

		final var buildBlock = CodeBlock.builder().add("return new $T", buildType);
		if (!interfaceType.parameters().isEmpty()) buildBlock.add("<>");
		buildBlock.add("(");
		boolean first = true;
		for (final var arg : arguments)
		{
			if (first) first = false;
			else buildBlock.add(", ");
			buildBlock.add(arg.argumentCodeBlock);
		}

		buildBlock.add(")");
		spec.addStatement(buildBlock.build());

		return spec.build();
	}

	private record BuildArgument(CodeBlock argumentCodeBlock, Optional<CodeBlock> preOperation)
	{
		public static BuildArgument of(FeatureResolution resolution)
		{
			final var feature = resolution.feature();
			final var name = feature.name();
			if (isSuppliedList(resolution))
			{
				final var newName = "built" + GenUtils.capitalizeFirstLetter(name);
				final var buildBlock = CodeBlock.of("final var $N = $T.collectSuppliers($N)",
													newName,
													ConstantTypes.BUILD_UTILS,
													name);
				final var arg = CodeBlock.of("$N", newName);
				return new BuildArgument(arg, Optional.of(buildBlock));
			}
			else if (feature instanceof Relation<?, ?> relation && !relation.lazy())
			{
				return new BuildArgument(CodeBlock.of("$N.get()", name), Optional.empty());

			}
			else
			{
				return new BuildArgument(CodeBlock.of("$N", name), Optional.empty());
			}
		}

		private static boolean isSuppliedList(FeatureResolution resolution)
		{
			final var feature = resolution.feature();
			return feature.many() && feature instanceof Relation<?, ?> relation && !relation.lazy();
		}
	}
}
