package org.logoce.lmf.generator.group.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupImplementationType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.OperationUtil;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.api.model.FeaturedObject;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.api.model.IModelNotifier;
import org.logoce.lmf.model.api.model.ModelNotifier;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.List;

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
		final var features = FeatureStreams.distinctFeatures(group).toList();
		final var featureCount = features.size();
		final var featuresType = resolveFeaturesType(interfaceType);
		final var classBuilder = implementationType.classSpecBuilder()
												   .superclass(resolveFeaturedSuperclass(featuresType))
												   .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
		installFeatureCount(classBuilder, featureCount);
		installNotifierField(classBuilder, featuresType);
		installNotifierMethod(classBuilder, featuresType);
		final var featureInstallers = ImplementationFeatureUtil.buildFeatureInstallers(classBuilder, group);
		final var typeInstallers = ImplementationFeatureUtil.buildTypeInstallers(interfaceType, classBuilder);

		features.stream()
				.map(f -> f.adapt(FeatureResolution.class))
				.forEach(featureInstallers::install);
		typeInstallers.install(group);

		installFeatureIndex(classBuilder, interfaceType, features);
		installOperationStubs(classBuilder);

		final var javaFile = JavaFile.builder(implementationType.raw().packageName(), classBuilder.build())
									 .skipJavaLangImports(true)
									 .build();
		FormattedJavaWriter.write(javaFile, targetDirectory);
	}

	private void installFeatureIndex(final TypeSpec.Builder classBuilder,
									 final GroupInterfaceType interfaceType,
									 final List<? extends Feature<?, ?, ?, ?>> features)
	{
		final var domainType = interfaceType.raw();
		// Instance override delegating to the static helper
		final var instanceMethod = MethodSpec.methodBuilder("featureIndex")
											 .addAnnotation(Override.class)
											 .addModifiers(Modifier.PUBLIC)
											 .returns(int.class)
											 .addParameter(int.class, "featureId")
											 .addStatement("return featureIndexStatic(featureId)");

		// Static helper carrying the actual switch
		final var staticBuilder = MethodSpec.methodBuilder("featureIndexStatic")
											.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
											.returns(int.class)
											.addParameter(int.class, "featureId");

		if (features.isEmpty())
		{
			staticBuilder.addStatement("throw new IllegalArgumentException($S + featureId)",
									   "Unknown featureId: ");
		}
		else
		{
			final var body = CodeBlock.builder();
			body.add("return switch (featureId) {\n");

			for (int i = 0; i < features.size(); i++)
			{
				final var feature = features.get(i);
				final var constantName = GenUtils.toConstantCase(feature.name());
				body.addStatement("  case $T.FeatureIDs.$N -> $L", domainType, constantName, i);
			}

			body.addStatement("  default -> throw new IllegalArgumentException($S + featureId)",
							  "Unknown featureId: ");
			body.add("};\n");

			staticBuilder.addCode(body.build());
		}

		classBuilder.addMethod(staticBuilder.build());
		classBuilder.addMethod(instanceMethod.build());
	}

	private void installOperationStubs(final TypeSpec.Builder classBuilder)
	{
		OperationUtil.collectOperations(group)
					  .stream()
					  .map(operation -> buildOperationStub(operation, group))
					  .forEach(classBuilder::addMethod);
	}

	private static TypeName resolveFeaturedSuperclass(final TypeName featuresType)
	{
		final var featuredType = TypeParameter.of(FEATURE_OBJECT_TYPE, featuresType);
		return featuredType.parametrized();
	}

	private static TypeName resolveFeaturesType(final GroupInterfaceType interfaceType)
	{
		return GenUtils.parameterize(interfaceType.raw().nestedClass("Features"), List.of(GenUtils.WILDCARD));
	}

	private static MethodSpec buildOperationStub(final Operation operation, final Group<?> owner)
	{
		final var methodBuilder = MethodSpec.methodBuilder(operation.name())
											.addAnnotation(Override.class)
											.addModifiers(Modifier.PUBLIC);

		final var returnType = OperationUtil.resolveReturnType(operation, owner);
		methodBuilder.returns(returnType);

		for (final OperationParameter parameter : operation.parameters())
		{
			final var parameterType = OperationUtil.resolveParameterType(parameter, owner);
			methodBuilder.addParameter(parameterType, parameter.name());
		}
		final var body = operation.content();
		if (body != null && !body.isBlank())
		{
			methodBuilder.addCode(body);
		}
		else
		{
			methodBuilder.addStatement("throw new $T($S)",
									   UnsupportedOperationException.class,
									   "Operation '" + operation.name() + "' is not implemented");
		}

		return methodBuilder.build();
	}

	private static void installFeatureCount(final TypeSpec.Builder classBuilder, final int featureCount)
	{
		final var field = FieldSpec.builder(int.class, "FEATURE_COUNT", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
								   .initializer("$L", featureCount)
								   .build();
		classBuilder.addField(field);
	}

	private void installNotifierField(final TypeSpec.Builder classBuilder, final TypeName featuresType)
	{
		final var notifierType = TypeParameter.of(ClassName.get(ModelNotifier.class), featuresType);
		final var notifierField = FieldSpec.builder(notifierType.parametrized(), "notifier", Modifier.PRIVATE, Modifier.FINAL)
										   .initializer("new $T<>(FEATURE_COUNT, this::featureIndex)",
														ClassName.get(ModelNotifier.class))
										   .build();
		classBuilder.addField(notifierField);
	}

	private void installNotifierMethod(final TypeSpec.Builder classBuilder, final TypeName featuresType)
	{
		final var notifierType = TypeParameter.of(ClassName.get(IModelNotifier.Impl.class), featuresType);
		final var method = MethodSpec.methodBuilder("notifier")
									 .addAnnotation(Override.class)
									 .addModifiers(Modifier.PUBLIC)
									 .returns(notifierType.parametrized())
									 .addStatement("return notifier")
									 .build();
		classBuilder.addMethod(method);
	}
}
