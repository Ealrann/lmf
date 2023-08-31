package isotropy.lmf.generator.model;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.model.feature.BuilderMethodBuilder;
import isotropy.lmf.generator.model.feature.FeatureResolution;
import isotropy.lmf.generator.model.feature.InternalFeatureBuilder;
import isotropy.lmf.generator.model.feature.MethodBuilder;
import isotropy.lmf.generator.util.GenUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class GroupGenerator
{
	private final Group<?> group;
	private final String packageName;

	public GroupGenerator(Group<?> group)
	{
		this.group = group;
		final var model = (Model) group.lmContainer();
		packageName = model.domain();
	}

	public void generate(final File target)
	{
		final var isFinal = group.concrete();
		final var includes = group.includes();
		final var refInclude = includes.isEmpty() ? null : includes.get(0);
		final var types = Types.build(refInclude, group);

		final var interfaceBuilder = TypeSpec.interfaceBuilder(types.className())
											 .addSuperinterface(types.superType())
											 .addTypeVariables(types.typedParameters())
											 .addModifiers(Modifier.PUBLIC);

		final var featureResolutions = ModelUtils.streamAllFeatures(group)
												 .map(FeatureResolution::from)
												 .toList();

		final var internalFeaturesInterfaceBuilder = TypeSpec.interfaceBuilder("Features")
															 .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		final var internalFeatureBuilder = new InternalFeatureBuilder(group);
		featureResolutions.stream()
						  .map(internalFeatureBuilder::toConstantFeature)
						  .forEach(internalFeaturesInterfaceBuilder::addField);

		interfaceBuilder.addType(internalFeaturesInterfaceBuilder.build());

		if (isFinal)
		{
			final var builderTypes = types.builder();
			final var builderInterface = buildBuilderInterface(builderTypes, featureResolutions);
			interfaceBuilder.addType(builderInterface);
		}

		buildFeatureMethods(featureResolutions).forEach(interfaceBuilder::addMethod);

		try
		{
			final var javaFile = JavaFile.builder(packageName, interfaceBuilder.build())
										 .build();
			javaFile.writeTo(target);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static Stream<MethodSpec> buildFeatureMethods(final List<FeatureResolution> featureResolutions)
	{
		final var methodBuilder = new MethodBuilder();
		return featureResolutions.stream()
								 .map(methodBuilder::build);
	}

	private static TypeSpec buildBuilderInterface(final Types builderTypes,
												  final List<FeatureResolution> featureResolutions)
	{
		final var typedBuilder = GenUtils.parameterize(builderTypes.className(), builderTypes.rawParameters());
		final var methodBuilder = new BuilderMethodBuilder(typedBuilder);

		final var builderTypeBuilder = TypeSpec.interfaceBuilder(builderTypes.className())
											   .addSuperinterface(builderTypes.superType())
											   .addTypeVariables(builderTypes.typedParameters())
											   .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		featureResolutions.stream()
						  .map(methodBuilder::build)
						  .forEach(builderTypeBuilder::addMethod);

		return builderTypeBuilder.build();
	}
}
