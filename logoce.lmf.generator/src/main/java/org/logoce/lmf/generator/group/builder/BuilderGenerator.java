package org.logoce.lmf.generator.group.builder;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupBuilderClassType;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public final class BuilderGenerator
{
	private final File targetDirectory;
	private final Group<?> group;

	public BuilderGenerator(final File targetDirectory, final Group<?> group)
	{
		this.targetDirectory = targetDirectory;
		this.group = group;
	}

	public void generate()
	{
		final var builderType = group.adapt(GroupBuilderClassType.class);
		final var packageName = builderType.packageName;
		final var typedInterface = builderType.parametrized();

		final var classBuilder = builderType.classSpecBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		final var defaultConstructor = MethodSpec.constructorBuilder()
												 .addModifiers(Modifier.PUBLIC)
												 .build();
		classBuilder.addMethod(defaultConstructor);

		final var featureInstallers = BuilderFeatureUtil.buildFeatureInstallers(classBuilder, typedInterface, group);
		final var typeInstallers = BuilderFeatureUtil.buildTypeInstallers(classBuilder, group);

		final var featureResolutions = FeatureStreams.distinctFeatures(group)
													 .map(f -> f.adapt(FeatureResolution.class))
													 .toList();

		featureResolutions.forEach(featureInstallers::install);

		typeInstallers.install(featureResolutions);

		final var javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
		FormattedJavaWriter.write(javaFile, targetDirectory);
	}
}
