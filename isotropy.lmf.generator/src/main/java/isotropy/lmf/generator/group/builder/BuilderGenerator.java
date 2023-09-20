package isotropy.lmf.generator.group.builder;

import com.squareup.javapoet.JavaFile;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.adapter.GroupResolution;

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
		final var context = group.adapt(GroupResolution.class);
		final var packageName = context.packageName + ".builder";
		final var builderType = context.interfaceType.builderClass();
		final var typedInterface = builderType.parametrized();

		final var classBuilder = builderType.classSpecBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		final var featureInstallers = BuilderFeatureUtil.buildFeatureInstallers(classBuilder, typedInterface);
		final var typeInstallers = BuilderFeatureUtil.buildTypeInstallers(classBuilder, context.interfaceType);

		final var featureResolutions = ModelUtils.streamAllFeatures(group)
												 .map(f -> f.adapt(FeatureResolution.class))
												 .toList();

		featureResolutions.forEach(featureInstallers::install);

		typeInstallers.install(featureResolutions);

		try
		{
			final var javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
			javaFile.writeTo(targetDirectory);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
