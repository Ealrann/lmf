package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupBuilderInterfaceType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.type.InterfaceBuildMethodBuilder;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.generator.util.OperationUtil;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Operation;
import org.logoce.lmf.model.lang.OperationParameter;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.util.List;

public final class InterfaceGenerator
{
	private static final boolean ENUM_NOTIFICATION_FEATURES = false;
	private static final FeatureMethodBuilder GETTER_METHOD_BUILDER = InterfaceMethodUtil.methodBuilder();
	private static final FeatureMethodBuilder SETTER_METHOD_BUILDER = InterfaceMethodUtil.setterMethodBuilder();
	private static final InterfaceBuildMethodBuilder buildMethod = new InterfaceBuildMethodBuilder();

	private final File targetDirectory;
	private final Group<?> group;

	public InterfaceGenerator(final File targetDirectory, final Group<?> group)
	{
		this.targetDirectory = targetDirectory;
		this.group = group;
	}

	public void generate()
	{
		final var groupType = group.adapt(GroupInterfaceType.class);
		final var packageName = groupType.packageName;
		final var isFinal = group.concrete();
		final var internalFeaturesGenerator = new InternalFeaturesGenerator(group);
		final var notificationFeaturesGenerator = new NotificationFeaturesGenerator(group);
		final var internalFeatures = internalFeaturesGenerator.build();
		final var notificationFeatures = notificationFeaturesGenerator.build();
		final var featureResolutions = ModelUtils.streamAllFeatures(group)
												 .map(f -> f.adapt(FeatureResolution.class))
												 .toList();

		final var interfaceBuilder = groupType.interfaceSpecBuilder()
											  .addModifiers(Modifier.PUBLIC)
											  .addType(internalFeatures);

		if (ENUM_NOTIFICATION_FEATURES) interfaceBuilder.addTypes(notificationFeatures);

		if (isFinal)
		{
			final var builderTypes = group.adapt(GroupBuilderInterfaceType.class);
			final var builderInterface = buildBuilderInterface(builderTypes, featureResolutions);
			interfaceBuilder.addType(builderInterface);
			interfaceBuilder.addMethod(buildMethod.build(group));
		}

		featureResolutions.stream()
						  .filter(this::matchGroup)
						  .map(GETTER_METHOD_BUILDER::build)
						  .forEach(interfaceBuilder::addMethod);

		featureResolutions.stream()
						  .filter(this::matchGroup)
						  .filter(InterfaceMethodUtil::isMutableSingle)
						  .map(SETTER_METHOD_BUILDER::build)
						  .forEach(interfaceBuilder::addMethod);

		final var operations = group.concrete()
								? OperationUtil.collectOperations(group)
								: group.operations();
		operations.forEach(operation -> interfaceBuilder.addMethod(buildOperationMethod(operation)));

		final var javaFile = JavaFile.builder(packageName, interfaceBuilder.build()).build();
		FormattedJavaWriter.write(javaFile, targetDirectory);
	}

	private boolean matchGroup(final FeatureResolution f)
	{
		return f.feature().lmContainer() == group;
	}

	private static TypeSpec buildBuilderInterface(final GroupBuilderInterfaceType builderType,
												  final List<FeatureResolution> featureResolutions)
	{
		final var typedBuilder = builderType.parametrized();
		final var methodBuilder = InterfaceMethodUtil.builderMethodBuilder(typedBuilder, builderType.group);
		final var manyRelationMethodBuilder = InterfaceMethodUtil.builderManyRelationListMethodBuilder(typedBuilder,
																										 builderType.group);

		final var builderTypeBuilder = builderType.interfaceSpecBuilder()
												  .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		featureResolutions.stream().map(methodBuilder::build).forEach(builderTypeBuilder::addMethod);
		featureResolutions.stream()
						  .filter(f -> f.feature() instanceof Relation<?, ?> relation && relation.many())
						  .map(manyRelationMethodBuilder::build)
						  .forEach(builderTypeBuilder::addMethod);

		return builderTypeBuilder.build();
	}

	private MethodSpec buildOperationMethod(final Operation operation)
	{
		final var methodBuilder = MethodSpec.methodBuilder(operation.name())
											.addModifiers(group.concrete() ? Modifier.ABSTRACT : Modifier.DEFAULT,
														  Modifier.PUBLIC);

		final var returnType = OperationUtil.resolveReturnType(operation, group);
		methodBuilder.returns(returnType);

		for (final OperationParameter parameter : operation.parameters())
		{
			final var parameterType = OperationUtil.resolveParameterType(parameter, group);
			final var parameterName = parameter.name();
			final var parameterSpec = ParameterSpec.builder(parameterType, parameterName).build();
			methodBuilder.addParameter(parameterSpec);
		}

		if (!group.concrete())
		{
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
		}

		return methodBuilder.build();
	}
}
