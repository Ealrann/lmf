package isotropy.lmf.generator.group.iface;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.adapter.GroupResolution;
import isotropy.lmf.generator.code.feature.FeatureMethodBuilder;
import isotropy.lmf.generator.code.type.InterfaceBuildMethodBuilder;
import isotropy.lmf.generator.util.GroupType;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;

public final class InterfaceGenerator
{
	private static final boolean ENUM_NOTIFICATION_FEATURES = false;
	public static final FeatureMethodBuilder METHOD_BUILDER = InterfaceMethodUtil.methodBuilder();

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
		final var context = group.adapt(GroupResolution.class);
		final var packageName = context.packageName;
		final var types = context.interfaceType;
		final var isFinal = group.concrete();
		final var internalFeaturesGenerator = new InternalFeaturesGenerator(group);
		final var notificationFeaturesGenerator = new NotificationFeaturesGenerator(group);
		final var internalFeatures = internalFeaturesGenerator.build();
		final var notificationFeatures = notificationFeaturesGenerator.build();
		final var featureResolutions = ModelUtils.streamAllFeatures(group)
												 .map(f -> f.adapt(FeatureResolution.class))
												 .toList();

		final var interfaceBuilder = types.interfaceSpecBuilder()
										  .addModifiers(Modifier.PUBLIC)
										  .addType(internalFeatures);

		if (ENUM_NOTIFICATION_FEATURES) interfaceBuilder.addTypes(notificationFeatures);

		if (isFinal)
		{
			final var builderTypes = types.builderInterface();
			final var builderInterface = buildBuilderInterface(builderTypes, featureResolutions);
			interfaceBuilder.addType(builderInterface);

			interfaceBuilder.addMethod(buildMethod.build(types));
		}

		featureResolutions.stream()
						  .filter(this::matchGroup)
						  .map(METHOD_BUILDER::build)
						  .forEach(interfaceBuilder::addMethod);

		try
		{
			final var javaFile = JavaFile.builder(packageName, interfaceBuilder.build()).build();
			javaFile.writeTo(targetDirectory);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private boolean matchGroup(final FeatureResolution f)
	{
		return f.feature().lmContainer() == group;
	}

	private static TypeSpec buildBuilderInterface(final GroupType builderType,
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
