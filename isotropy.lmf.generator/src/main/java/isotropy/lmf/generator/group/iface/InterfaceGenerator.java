package isotropy.lmf.generator.group.iface;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.code.feature.FeatureMethodBuilder;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.feature.InternalFeatureBuilder;
import isotropy.lmf.generator.code.type.InterfaceBuildMethodBuilder;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.util.GroupType;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

public final class InterfaceGenerator
{
	public static final FeatureMethodBuilder METHOD_BUILDER = InterfaceMethodUtil.methodBuilder();

	private static final InterfaceBuildMethodBuilder buildMethod = new InterfaceBuildMethodBuilder();

	private final GroupGenerationContext context;

	public InterfaceGenerator(final GroupGenerationContext context)
	{
		this.context = context;
	}

	public void generate()
	{
		final var group = context.group();
		final var packageName = context.packageName();
		final var featureResolutions = context.featureResolutions();
		final var types = context.interfaceType();
		final var isFinal = group.concrete();
		final var internalFeaturesInterface = buildInternalFeaturesInterface(group, featureResolutions);

		final var interfaceBuilder = types.interfaceSpecBuilder()
										  .addModifiers(Modifier.PUBLIC)
										  .addType(internalFeaturesInterface);

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
			javaFile.writeTo(context.interfaceDirectory());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static TypeSpec buildInternalFeaturesInterface(final Group<?> group,
														   final List<FeatureResolution> featureResolutions)
	{
		final var internalFeaturesInterfaceBuilder = TypeSpec.interfaceBuilder("Features")
															 .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		final var internalFeatureBuilder = new InternalFeatureBuilder(group);
		featureResolutions.stream()
						  .map(internalFeatureBuilder::toConstantFeature)
						  .forEach(internalFeaturesInterfaceBuilder::addField);

		return internalFeaturesInterfaceBuilder.build();
	}

	private boolean matchGroup(final FeatureResolution f)
	{
		return f.feature().lmContainer() == context.group();
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
