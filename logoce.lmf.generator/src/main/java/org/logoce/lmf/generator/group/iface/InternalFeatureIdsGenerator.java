package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.ParameterizedTypeName;
import org.logoce.lmf.generator.code.model.FeatureIdFieldBuilder;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.notification.api.IFeatures;

import javax.lang.model.element.Modifier;

public final class InternalFeatureIdsGenerator
{
	private final Group<?> group;

	public InternalFeatureIdsGenerator(final Group<?> group)
	{
		this.group = group;
	}

	public TypeSpec build()
	{
		final var featureIdsInterfaceBuilder = TypeSpec.interfaceBuilder("FeatureIDs")
													   .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		// Move the self-typed IFeatures hierarchy to FeatureIDs.
		final var hasSuperInterfaces = !group.includes().isEmpty();
		final var selfType = ClassName.get("", "FeatureIDs");
		final var selfBound = ParameterizedTypeName.get(selfType, TypeVariableName.get("T"));
		final var variableTypeName = TypeVariableName.get("T", selfBound);

		featureIdsInterfaceBuilder.addTypeVariable(variableTypeName);

		if (hasSuperInterfaces)
		{
			for (final var superInterface : group.includes())
			{
				final var parentGroup = superInterface.group();
				final var sameMetaModel = parentGroup.lmContainer() == group.lmContainer();
				final var parentIds = ClassName.get("", parentGroup.name() + ".FeatureIDs");

				if (sameMetaModel)
				{
					final var typed = TypeParameter.of(parentIds, variableTypeName);
					featureIdsInterfaceBuilder.addSuperinterface(typed.parametrized());
				}
				else
				{
					featureIdsInterfaceBuilder.addSuperinterface(parentIds);
				}
			}
		}
		else if (group.name().equals("LMObject"))
		{
			final var iFeatures = ClassName.get(IFeatures.class);
			final var typed = TypeParameter.of(iFeatures, variableTypeName);
			featureIdsInterfaceBuilder.addSuperinterface(typed.parametrized());
		}
		else
		{
			final var baseIds = ClassName.get(LMObject.FeatureIDs.class);
			featureIdsInterfaceBuilder.addSuperinterface(baseIds);
		}

		final var fieldBuilder = new FeatureIdFieldBuilder(group);

		FeatureStreams.distinctFeatures(group)
					  .map(fieldBuilder::build)
					  .forEach(featureIdsInterfaceBuilder::addField);

		return featureIdsInterfaceBuilder.build();
	}
}
