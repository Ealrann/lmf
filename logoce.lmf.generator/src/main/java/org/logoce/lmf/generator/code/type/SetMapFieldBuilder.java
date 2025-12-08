package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupImplementationType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.adapter.ModelResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.ModelUtil;

import javax.lang.model.element.Modifier;

public class SetMapFieldBuilder implements CodeBuilder<Group<?>, FieldSpec>
{
	public static final TypeParameter SETTER_MAP_CLASS = TypeParameter.of(FeatureSetter.class);
	public static final TypeParameter SETTER_MAP_BUILDER_CLASS = TypeParameter.of(FeatureSetter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};

	private final GroupInterfaceType interfaceType;

	public SetMapFieldBuilder(final GroupInterfaceType interfaceType)
	{
		this.interfaceType = interfaceType;
	}

	@Override
	public FieldSpec build(final Group<?> group)
	{
		final var type = SETTER_MAP_CLASS.nest(interfaceType.parametrizedWildcard());
		final var builderType = SETTER_MAP_BUILDER_CLASS.nest(interfaceType.parametrizedWildcard());
		final var implementationType = group.adapt(GroupImplementationType.class);
		final var initializerBuilder = CodeBlock.builder()
												.add("new $T()", builderType.parametrized());

		FeatureStreams.distinctFeatures(group)
					  .filter(SetMapFieldBuilder::isSingleMutable)
					  .map(f -> f.adapt(FeatureResolution.class))
					  .map(resolution -> buildStatement(resolution, implementationType))
					  .forEach(initializerBuilder::add);

		initializerBuilder.add(".build()");

		return FieldSpec.builder(type.parametrized(), "SET_MAP")
						.addModifiers(modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	private CodeBlock buildStatement(final FeatureResolution resolution,
									 final GroupImplementationType implementationType)
	{
		final var featureName = resolution.name();
		final var ownerGroup = interfaceType.group;
		final var group = resolution.hasGeneric() && resolution.feature().lmContainer() != ownerGroup
						 ? ownerGroup
						 : (Group<?>) resolution.feature().lmContainer();
		final var constantGroupName = GenUtils.toConstantCase(group.name());

		if (GenUtils.USE_RAWFEATURE_FOR_MODEL)
		{
			return CodeBlock.of(".add($T.Features.$N, (object, value) -> (($T) object).$N(value))",
								interfaceType.raw(),
								featureName,
								implementationType.raw(),
								featureName);
		}
		else
		{
			final var model = (MetaModel) ModelUtil.root(group);
			final var modelDefinition = model.adapt(ModelResolution.class).modelDefinition;
			return CodeBlock.of(".add($T.Features.$N.$N, (object, value) -> (($T) object).$N(value))",
								modelDefinition,
								constantGroupName,
								GenUtils.toConstantCase(featureName),
								implementationType.raw(),
								featureName);
		}
	}

	private static boolean isSingleMutable(final Feature<?, ?> feature)
	{
		return !feature.immutable() && !feature.many();
	}
}
