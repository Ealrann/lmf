package isotropy.lmf.generator.group.iface;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.feature.InternalFeatureBuilder;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.util.TypeParameter;
import org.logoce.notification.api.IFeatures;

import javax.lang.model.element.Modifier;
import java.util.List;

public class InternalFeaturesGenerator
{
	private final Group<?> group;
	private final List<FeatureResolution> featureResolutions;

	public InternalFeaturesGenerator(final GroupGenerationContext context)
	{
		this.group = context.group();
		this.featureResolutions = context.featureResolutions();
	}

	public TypeSpec build()
	{
		final var internalFeaturesInterfaceBuilder = TypeSpec.interfaceBuilder("Features")
															 .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		final var hasSuperInterfaces = !group.includes().isEmpty();
		final var concrete = group.concrete();
		final var superVType = concrete ? "Features" : "T";

		if (!concrete)
		{
			final var variableTypeName = TypeVariableName.get("T extends Features<T>");
			internalFeaturesInterfaceBuilder.addTypeVariable(variableTypeName);
		}

		if (hasSuperInterfaces)
		{
			for (final var superInterface : group.includes())
			{
				final var iFeatures = ClassName.get("", superInterface.group().name() + ".Features");
				final var typed = TypeParameter.of(iFeatures, ClassName.get("", superVType));
				internalFeaturesInterfaceBuilder.addSuperinterface(typed.parametrized());
			}
		}
		else if (group.name().equals("LMObject"))
		{
			final var iFeatures = ClassName.get(IFeatures.class);
			final var typed = TypeParameter.of(iFeatures, ClassName.get("", superVType));
			internalFeaturesInterfaceBuilder.addSuperinterface(typed.parametrized());
		}
		else
		{
			final var iFeatures = ClassName.get(LMObject.Features.class);
			final var typed = TypeParameter.of(iFeatures, ClassName.get("", superVType));
			internalFeaturesInterfaceBuilder.addSuperinterface(typed.parametrized());
		}

		final var internalFeatureBuilder = new InternalFeatureBuilder(group);
		featureResolutions.stream()
						  .map(internalFeatureBuilder::toConstantFeature)
						  .forEach(internalFeaturesInterfaceBuilder::addField);

		return internalFeaturesInterfaceBuilder.build();
	}
}
