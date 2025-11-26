package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.feature.InternalFeatureBuilder;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.util.ModelUtils;
import org.logoce.lmf.notification.api.IFeatures;

import javax.lang.model.element.Modifier;

public class InternalFeaturesGenerator
{
	private final Group<?> group;

	public InternalFeaturesGenerator(final Group<?> group)
	{
		this.group = group;
	}

	public TypeSpec build()
	{
		final var internalFeaturesInterfaceBuilder = TypeSpec.interfaceBuilder("Features")
															 .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
		final var hasSuperInterfaces = !group.includes().isEmpty();
		final var featuresClassName = ClassName.get("", "Features");
		final var variableTypeName = TypeVariableName.get("T",
														  ParameterizedTypeName.get(featuresClassName,
																				   TypeVariableName.get("T")));

		internalFeaturesInterfaceBuilder.addTypeVariable(variableTypeName);

		if (hasSuperInterfaces)
		{
			for (final var superInterface : group.includes())
			{
				final var iFeatures = ClassName.get("", superInterface.group().name() + ".Features");
				final var typed = TypeParameter.of(iFeatures, variableTypeName);
				internalFeaturesInterfaceBuilder.addSuperinterface(typed.parametrized());
			}
		}
		else if (group.name().equals("LMObject"))
		{
			final var iFeatures = ClassName.get(IFeatures.class);
			final var typed = TypeParameter.of(iFeatures, variableTypeName);
			internalFeaturesInterfaceBuilder.addSuperinterface(typed.parametrized());
		}
		else
		{
			final var iFeatures = ClassName.get(LMObject.Features.class);
			final var typed = TypeParameter.of(iFeatures, variableTypeName);
			internalFeaturesInterfaceBuilder.addSuperinterface(typed.parametrized());
		}

		final var internalFeatureBuilder = new InternalFeatureBuilder(group);

		ModelUtils.streamAllFeatures(group)
				  .map(f -> f.adapt(FeatureResolution.class))
				  .map(internalFeatureBuilder::toConstantFeature)
				  .forEach(internalFeaturesInterfaceBuilder::addField);

		return internalFeaturesInterfaceBuilder.build();
	}
}
