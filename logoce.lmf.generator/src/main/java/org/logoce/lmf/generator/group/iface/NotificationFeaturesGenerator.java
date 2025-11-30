package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.code.feature.NotificationFeatureBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.model.api.feature.INotificationFeature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;

import javax.lang.model.element.Modifier;
import java.util.List;

public class NotificationFeaturesGenerator
{
	private final Group<?> group;

	public NotificationFeaturesGenerator(final Group<?> group)
	{
		this.group = group;
	}

	public List<TypeSpec> build()
	{
		final var featureInterfaceType = ClassName.get("", "NotificationFeature");
		final var featureInterfaceBuilder = TypeSpec.interfaceBuilder(featureInterfaceType)
													.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		final var hasSuperInterfaces = !group.includes().isEmpty();

		if (hasSuperInterfaces)
		{
			for (final var superInterface : group.includes())
			{
				final var notificationFeature = ClassName.get("",
															  superInterface.group().name() + ".NotificationFeature");
				featureInterfaceBuilder.addSuperinterface(notificationFeature);
			}
		}
		else if (group.name().equals("LMObject"))
		{
			final var notificationFeature = ClassName.get(INotificationFeature.class);
			featureInterfaceBuilder.addSuperinterface(notificationFeature);
		}
		else
		{
			final var notificationFeature = ClassName.get(LMObject.Features.class);
			featureInterfaceBuilder.addSuperinterface(notificationFeature);
		}

		if (!group.features().isEmpty())
		{
			final var localType = ClassName.get(INotificationFeature.class);
			final var featuresEnumBuilder = TypeSpec.enumBuilder("NotificationFeatures")
													.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
													.addSuperinterface(featureInterfaceType);
			final var notificationFeatureBuilder = new NotificationFeatureBuilder(group);
			final var enumConstantSpecs = FeatureStreams.distinctFeatures(group)
														.map(notificationFeatureBuilder::buildSpec)
														.toList();

			final var anyLocal = enumConstantSpecs.stream().anyMatch(NotificationFeatureBuilder.EnumSpec::local);
			final var allLocal = enumConstantSpecs.stream().allMatch(NotificationFeatureBuilder.EnumSpec::local);

			enumConstantSpecs.forEach(n -> n.insert(featuresEnumBuilder));

			if (allLocal)
			{
				featuresEnumBuilder.addMethod(MethodSpec.methodBuilder("root")
														.addModifiers(Modifier.PUBLIC)
														.returns(localType)
														.addStatement("return null")
														.addAnnotation(ConstantTypes.OVERRIDE)
														.build());
			}
			else
			{
				featuresEnumBuilder.addField(FieldSpec.builder(localType, "root", Modifier.PUBLIC, Modifier.FINAL)
													  .build());

				featuresEnumBuilder.addMethod(MethodSpec.constructorBuilder()
														.addParameter(localType, "root")
														.addStatement("this.root = root")
														.build());

				if (anyLocal)
				{
					featuresEnumBuilder.addMethod(MethodSpec.constructorBuilder()
															.addStatement("this.root = null")
															.build());
				}

				featuresEnumBuilder.addMethod(MethodSpec.methodBuilder("root")
														.addModifiers(Modifier.PUBLIC)
														.returns(localType)
														.addStatement("return root")
														.addAnnotation(ConstantTypes.OVERRIDE)
														.build());
			}

			return List.of(featureInterfaceBuilder.build(), featuresEnumBuilder.build());
		}
		else
		{
			return List.of(featureInterfaceBuilder.build());
		}
	}
}
