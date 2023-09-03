package isotropy.lmf.generator.group.iface;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.group.feature.FeatureResolution;
import isotropy.lmf.generator.group.feature.InternalFeatureBuilder;
import isotropy.lmf.generator.group.feature.MethodBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.Types;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.List;

public final class InterfaceGenerator
{
	public static final MethodBuilder METHOD_BUILDER = InterfaceMethodUtil.methodBuilder();
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
		final var types = context.types();
		final var isFinal = group.concrete();

		final var interfaceBuilder = TypeSpec.interfaceBuilder(types.interfaceName())
											 .addSuperinterface(types.superInterface())
											 .addTypeVariables(types.detailedParameters())
											 .addModifiers(Modifier.PUBLIC);

		final var internalFeaturesInterface = buildInternalFeaturesInterface(group, featureResolutions);
		interfaceBuilder.addType(internalFeaturesInterface);

		if (isFinal)
		{
			final var builderTypes = types.builder();
			final var builderInterface = buildBuilderInterface(builderTypes, featureResolutions);
			interfaceBuilder.addType(builderInterface);
		}

		featureResolutions.stream()
						  .filter(this::matchGroup)
						  .map(METHOD_BUILDER::build)
						  .forEach(interfaceBuilder::addMethod);

		try
		{
			final var javaFile = JavaFile.builder(packageName, interfaceBuilder.build())
										 .build();
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

		final var internalFeaturesInterface = internalFeaturesInterfaceBuilder.build();
		return internalFeaturesInterface;
	}

	private boolean matchGroup(final FeatureResolution f)
	{
		return f.feature()
				.lmContainer() == context.group();
	}

	private static TypeSpec buildBuilderInterface(final Types builderTypes,
												  final List<FeatureResolution> featureResolutions)
	{
		final var typedBuilder = GenUtils.parameterize(builderTypes.interfaceName(), builderTypes.finalParameters());
		final var methodBuilder = InterfaceMethodUtil.builderMethodBuilder(typedBuilder);

		final var builderTypeBuilder = TypeSpec.interfaceBuilder(builderTypes.interfaceName())
											   .addSuperinterface(builderTypes.superInterface())
											   .addTypeVariables(builderTypes.detailedParameters())
											   .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		featureResolutions.stream()
						  .map(methodBuilder::build)
						  .forEach(builderTypeBuilder::addMethod);

		return builderTypeBuilder.build();
	}
}
