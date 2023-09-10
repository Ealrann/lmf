package isotropy.lmf.generator.group.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import isotropy.lmf.core.api.model.FeaturedObject;
import isotropy.lmf.generator.group.GroupGenerationContext;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public final class ImplementationGenerator
{
	public static final ClassName FEATURE_OBJECT_TYPE = ClassName.get(FeaturedObject.class);
	private final GroupGenerationContext context;

	public ImplementationGenerator(final GroupGenerationContext context)
	{
		this.context = context;
	}

	public void generate()
	{
		final var featureResolutions = context.featureResolutions();
		final var types = context.interfaceType();
		final var implementationType = types.implementation();
		final var classBuilder = implementationType.classSpecBuilder()
												   .superclass(FEATURE_OBJECT_TYPE)
												   .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		final var featureInstallers = ImplementationFeatureUtil.buildFeatureInstallers(classBuilder);
		final var typeInstallers = ImplementationFeatureUtil.buildTypeInstallers(classBuilder);

		featureResolutions.forEach(featureInstallers::install);
		typeInstallers.install(context);

		try
		{
			final var javaFile = JavaFile.builder(implementationType.raw().packageName(), classBuilder.build()).build();
			javaFile.writeTo(context.interfaceDirectory());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
