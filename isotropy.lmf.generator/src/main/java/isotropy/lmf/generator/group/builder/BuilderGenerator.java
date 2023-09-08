package isotropy.lmf.generator.group.builder;

import com.squareup.javapoet.JavaFile;
import isotropy.lmf.generator.group.GroupGenerationContext;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public final class BuilderGenerator
{
	private final GroupGenerationContext context;

	public BuilderGenerator(final GroupGenerationContext context)
	{
		this.context = context;
	}

	public void generate()
	{
		final var packageName = context.packageName() + ".builder";
		final var featureResolutions = context.featureResolutions();
		final var builderType = context.interfaceType().builderClass();
		final var typedInterface = builderType.parametrized();

		final var classBuilder = builderType.classSpecBuilder().addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		final var featureInstallers = BuilderFeatureUtil.buildFeatureInstallers(classBuilder, typedInterface);
		final var typeInstallers = BuilderFeatureUtil.buildTypeInstallers(classBuilder, context.interfaceType());

		featureResolutions.forEach(featureInstallers::install);
		typeInstallers.install(context.featureResolutions());

		try
		{
			final var javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
			javaFile.writeTo(context.interfaceDirectory());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
