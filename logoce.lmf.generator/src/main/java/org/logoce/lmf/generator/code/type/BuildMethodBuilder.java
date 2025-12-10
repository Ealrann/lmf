package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupImplementationType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public class BuildMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	private final TypeName buildType;
	private final GroupInterfaceType interfaceType;

	public BuildMethodBuilder(final Group<?> group)
	{
		this.interfaceType = group.adapt(GroupInterfaceType.class);
		this.buildType = group.adapt(GroupImplementationType.class).parametrized();
	}

	@Override
	public MethodSpec build(final List<FeatureResolution> context)
	{
		final var spec = MethodSpec.methodBuilder("build")
								   .addModifiers(Modifier.PUBLIC)
								   .returns(interfaceType.parametrized())
								   .addAnnotation(Override.class);

		final var arguments = context.stream()
									 .map(BuildArgument::of)
									 .toList();

		arguments.stream()
				 .map(BuildArgument::preOperation)
				 .filter(Optional::isPresent)
				 .map(Optional::get)
				 .forEach(spec::addStatement);
		final var constructorArguments = arguments.stream()
												 .filter(BuildArgument::isConstructorArgument)
												 .map(BuildArgument::argumentCodeBlock)
												 .collect(CodeBlock.joining(", "));

		final var constructorBlock = CodeBlock.builder()
											  .add("final var built = new $T($L)", buildType, constructorArguments);
		spec.addStatement(constructorBlock.build());

		arguments.stream()
				 .filter(arg -> !arg.isConstructorArgument())
				 .forEach(argument ->
				 {
					 final var name = argument.featureName();
					 if (argument.isMany())
					 {
						 spec.addStatement("built.$N().addAll($L)", name, argument.argumentCodeBlock());
					 }
					 else
					 {
						 spec.addStatement("built.$N($L)", name, argument.argumentCodeBlock());
					 }
				 });

		spec.addStatement("return built");

		return spec.build();
	}

	private record BuildArgument(FeatureResolution resolution,
								 CodeBlock argumentCodeBlock,
								 Optional<CodeBlock> preOperation)
	{
		public static BuildArgument of(final FeatureResolution resolution)
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
				return new BuildArgument(resolution, arg, Optional.of(buildBlock));
			}
			else if (feature instanceof Relation<?, ?, ?, ?> relation && !relation.lazy())
			{
				return new BuildArgument(resolution, CodeBlock.of("$N.get()", name), Optional.empty());

			}
			else
			{
				return new BuildArgument(resolution, CodeBlock.of("$N", name), Optional.empty());
			}
		}

		public boolean isConstructorArgument()
		{
			final var feature = resolution.feature();
			return feature.immutable() || feature.mandatory();
		}

		public boolean isMany()
		{
			return resolution.feature().many();
		}

		public String featureName()
		{
			return resolution.feature().name();
		}

		private static boolean isSuppliedList(FeatureResolution resolution)
		{
			final var feature = resolution.feature();
			return feature.many() && feature instanceof Relation<?, ?, ?, ?> relation && !relation.lazy();
		}
	}
}
