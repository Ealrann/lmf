package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupBuilderInterfaceType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.type.InterfaceBuildMethodBuilder;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.model.lang.Group;
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
		final var methodBuilder = InterfaceMethodUtil.builderMethodBuilder(typedBuilder);

		final var builderTypeBuilder = builderType.interfaceSpecBuilder()
												  .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		featureResolutions.stream().map(methodBuilder::build).forEach(builderTypeBuilder::addMethod);

		return builderTypeBuilder.build();
	}
}
