package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.adapter.ModelResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.ModelUtils;

import javax.lang.model.element.Modifier;

public class GetMapFieldBuilder implements CodeBuilder<Group<?>, FieldSpec>
{
	public static final TypeParameter GETTER_MAP_CLASS = TypeParameter.of(FeatureGetter.class);
	public static final TypeParameter GETTER_MAP_BUILDER_CLASS = TypeParameter.of(FeatureGetter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};
	private final GroupInterfaceType interfaceType;

	public GetMapFieldBuilder(final GroupInterfaceType interfaceType)
	{
		this.interfaceType = interfaceType;
	}

	@Override
	public FieldSpec build(final Group<?> group)
	{
		final var wildcardInterface = interfaceType.parametrizedWildcard();
		final var type = GETTER_MAP_CLASS.nest(wildcardInterface);
		final var builderType = GETTER_MAP_BUILDER_CLASS.nest(wildcardInterface);
		final var statementBuilder = new StringBuilder();
		statementBuilder.append("new $T()");

		ModelUtils.streamAllFeatures(group)
				  .map(f -> f.adapt(FeatureResolution.class))
				  .map(this::buildStatement)
				  .forEach(statementBuilder::append);
		statementBuilder.append(".build()");

		return FieldSpec.builder(type.parametrized(), "GET_MAP")
						.addModifiers(modifiers)
						.initializer(statementBuilder.toString(), builderType.parametrized())
						.build();
	}

	private CodeBlock buildStatement(final FeatureResolution resolution)
	{
		final var featureName = resolution.name();
		final var ownerGroup = interfaceType.group;
		final var group = resolution.hasGeneric() && resolution.feature().lmContainer() != ownerGroup
						 ? ownerGroup
						 : (Group<?>) resolution.feature().lmContainer();
		final var constantGroupName = GenUtils.toConstantCase(group.name());

		if (GenUtils.USE_RAWFEATURE_FOR_MODEL)
		{
			return CodeBlock.of(".add($T.Features.$N, $T::$N)",
								interfaceType.raw(),
								featureName,
								interfaceType.raw(),
								featureName);
		}
		else
		{
			final var model = (MetaModel) ModelUtils.root(group);
			final var modelDefinition = model.adapt(ModelResolution.class).modelDefinition;
			return CodeBlock.of(".add($T.Features.$N.$N, $T::$N)",
								modelDefinition,
								constantGroupName,
								GenUtils.toConstantCase(featureName),
								interfaceType.raw(),
								featureName);
		}
	}
}
