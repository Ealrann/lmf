package isotropy.lmf.generator.group.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.generator.code.type.TypeFeatures;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.io.IOException;

public final class ImplementationGenerator
{
	public static final ClassName FEATURE_OBJECT_TYPE = ClassName.get("isotropy.lmf.core.model", "FeaturedObject");
	private final GroupGenerationContext context;

	public ImplementationGenerator(final GroupGenerationContext context)
	{
		this.context = context;
	}

	public void generate()
	{
		final var group = context.group();
		final var packageName = context.packageName() + ".impl";
		final var featureResolutions = context.featureResolutions();
		final var types = context.types();
		final var typedInterface = TypeParameter.of(types.interfaceName(), types.finalParameters());
		final var typeFeatures = new TypeFeatures(group, typedInterface, featureResolutions);
		final var className = ClassName.get(packageName, group.name() + "Impl");

		final var classBuilder = TypeSpec.classBuilder(className)
										 .addSuperinterface(typedInterface.parametrized())
										 .superclass(FEATURE_OBJECT_TYPE)
										 .addTypeVariables(types.detailedParameters())
										 .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		final var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		final var featureInstallers = ImplementationFeatureUtil.buildFeatureInstallers(classBuilder, constructor);
		final var typeInstallers = ImplementationFeatureUtil.buildTypeInstallers(classBuilder);

		featureResolutions.forEach(featureInstallers::install);
		typeInstallers.install(typeFeatures);

		try
		{
			classBuilder.addMethod(constructor.build());
			final var javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
			javaFile.writeTo(context.interfaceDirectory());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
