package org.logoce.lmf.generator.group.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupImplementationType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.util.ModelUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public final class ImplementationGenerator
{
	public static final ClassName FEATURE_OBJECT_TYPE = ClassName.get(FeaturedObject.class);
	private final File targetDirectory;
	private final Group<?> group;

	public ImplementationGenerator(final File targetDirectory, final Group<?> group)
	{
		this.targetDirectory = targetDirectory;
		this.group = group;
	}

	public void generate()
	{
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var implementationType = group.adapt(GroupImplementationType.class);
		final var classBuilder = implementationType.classSpecBuilder()
												   .superclass(FEATURE_OBJECT_TYPE)
												   .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		final var featureInstallers = ImplementationFeatureUtil.buildFeatureInstallers(classBuilder);
		final var typeInstallers = ImplementationFeatureUtil.buildTypeInstallers(interfaceType, classBuilder);

		ModelUtils.streamAllFeatures(group)
				  .map(f -> f.adapt(FeatureResolution.class))
				  .forEach(featureInstallers::install);
		typeInstallers.install(group);

		try
		{
			final var javaFile = JavaFile.builder(implementationType.raw().packageName(), classBuilder.build()).build();
			javaFile.writeTo(targetDirectory);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
