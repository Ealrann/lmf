package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.generator.code.model.FeaturesFieldBuilder;
import org.logoce.lmf.generator.code.util.FieldBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.notification.api.IFeatures;

public final class GroupFeaturesGenerator
{
	private final Group<?> group;

	public GroupFeaturesGenerator(final Group<?> group)
	{
		this.group = group;
	}

	public TypeSpec build()
	{
		final var fieldBuilder = new FeaturesFieldBuilder(group);
		final var baseInterface = new FieldBuilder<Group<?>>("Features",
															 fieldBuilder,
															 FeatureStreams::distinctFeatures,
															 ConstantTypes.FEATURE_ALL_BUILDER).build(group);

		final var featuresBuilder = baseInterface.toBuilder();

		final var hasSuperInterfaces = !group.includes().isEmpty();
		final var selfType = ClassName.get("", "Features");
		final var selfBound = ParameterizedTypeName.get(selfType, TypeVariableName.get("T"));
		final var variableTypeName = TypeVariableName.get("T", selfBound);

		featuresBuilder.addTypeVariable(variableTypeName);

		if (hasSuperInterfaces)
		{
			for (final var superInterface : group.includes())
			{
				final var parentGroup = superInterface.group();
				final var sameMetaModel = parentGroup.lmContainer() == group.lmContainer();
				final var parentFeatures = ClassName.get("", parentGroup.name() + ".Features");

				if (sameMetaModel)
				{
					final var typed = TypeParameter.of(parentFeatures, variableTypeName);
					featuresBuilder.addSuperinterface(typed.parametrized());
				}
				else
				{
					featuresBuilder.addSuperinterface(parentFeatures);
				}
			}
		}
		else if (group.name().equals("LMObject"))
		{
			final var iFeatures = ClassName.get(IFeatures.class);
			final var typed = TypeParameter.of(iFeatures, variableTypeName);
			featuresBuilder.addSuperinterface(typed.parametrized());
		}
		else
		{
			final var baseFeatures = ClassName.get(LMObject.class).nestedClass("Features");
			final var typed = TypeParameter.of(baseFeatures, variableTypeName);
			featuresBuilder.addSuperinterface(typed.parametrized());
		}

		return featuresBuilder.build();
	}
}
