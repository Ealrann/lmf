package org.logoce.lmf.generator.code.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.api.feature.RawFeature;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelUtil;

import javax.lang.model.element.Modifier;

public class InternalFeatureBuilder
{
	private static final ClassName RAW_FEATURE = ClassName.get(RawFeature.class);
	private final Group<?> group;

	public InternalFeatureBuilder(Group<?> group)
	{
		this.group = group;
	}

	public FieldSpec toConstantFeatureWithConsumer(FeatureResolution featureResolution)
	{
		final var feature = featureResolution.feature();
		final var callbackType = featureResolution.notificationCallbackType();
		final var parent = (Group<?>) feature.lmContainer();
		final var local = parent == group;
		final var specialize = featureResolution.requiresOwnerSpecialization(group);
		final var concrete = group.concrete();
		final var featuresType = local
								 ? concrete ? ClassName.get("", "RFeatures") : ClassName.get("", "RFeatures<?>")
								 : ClassName.get("", parent.name() + ".RFeatures<?>");
		final var type = ParameterizedTypeName.get(ClassName.get(RawFeature.class),
												   callbackType.box(),
												   featuresType.box());

		final var initializer = local || specialize
								? localInitializer(feature, group)
								: parentInitializer(feature);

		return FieldSpec.builder(type, feature.name())
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.initializer(initializer)
						.build();
	}

	public FieldSpec toConstantFeature(FeatureResolution featureResolution)
	{
		final var feature = featureResolution.feature();
		final var specialize = featureResolution.requiresOwnerSpecialization(group);
		final var targetGroup = specialize ? group : (Group<?>) feature.lmContainer();
		final var singleType = featureResolution.rawSingleTypeFor(targetGroup).parametrizedWildcard();
		final var effectiveType = featureResolution.rawEffectiveTypeFor(targetGroup).parametrizedWildcard();

		final var type = ParameterizedTypeName.get(ClassName.get(RawFeature.class),
												   singleType.box(),
												   effectiveType.box());

		final var initializer = feature.lmContainer() == group || specialize
								? localInitializer(feature, group)
								: parentInitializer(feature);

		return FieldSpec.builder(type, feature.name())
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.initializer(initializer)
						.build();
	}

	private static CodeBlock localInitializer(final Feature<?, ?> feature, final Group<?> owner)
	{
		final var model = (MetaModel) ModelUtil.root(owner);
		final var groupClass = ClassName.get(TargetPathUtil.packageName(model), owner.name());
		final var many = feature.many();
		final var relation = feature instanceof Relation<?, ?>;

		return CodeBlock.of("new $T<>($L,$L,() -> $T.Features.$N)",
							RAW_FEATURE,
							many,
							relation,
							groupClass,
							GenUtils.toConstantCase(feature.name()));
	}

	private static CodeBlock parentInitializer(final Feature<?, ?> feature)
	{
		final var group = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(group);
		final var groupClass = ClassName.get(TargetPathUtil.packageName(model), group.name());
		return CodeBlock.of("$T.RFeatures.$N", groupClass, feature.name());
	}
}
