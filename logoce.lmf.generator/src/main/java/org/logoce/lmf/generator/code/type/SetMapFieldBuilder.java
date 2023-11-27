package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.adapter.ModelResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.util.ModelUtils;

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
		final var statementBuilder = new StringBuilder();
		statementBuilder.append("new $T()");

		ModelUtils.streamAllFeatures(group)
				  .filter(SetMapFieldBuilder::isSingleMutable)
				  .map(f -> f.adapt(FeatureResolution.class))
				  .map(this::buildStatement)
				  .forEach(statementBuilder::append);

		statementBuilder.append(".build()");

		return FieldSpec.builder(type.parametrized(), "SET_MAP")
						.addModifiers(modifiers)
						.initializer(statementBuilder.toString(), builderType.parametrized())
						.build();
	}

	private CodeBlock buildStatement(final FeatureResolution resolution)
	{
		final var featureName = resolution.name();
		final var group = (Group<?>) resolution.feature().lmContainer();
		final var constantGroupName = GenUtils.toConstantCase(group.name());

		if (GenUtils.USE_RAWFEATURE_FOR_MODEL)
		{
			return CodeBlock.of(".add($T.Features.$N, $T::$N)",
								interfaceType.raw(),
								featureName,
								constantGroupName,
								featureName);
		}
		else
		{
			final var model = (MetaModel) ModelUtils.root(resolution.feature());
			final var modelDefinition = model.adapt(ModelResolution.class).modelDefinition;
			return CodeBlock.of(".add($T.Features.$N.$N, $T::$N)",
								modelDefinition,
								constantGroupName,
								GenUtils.toConstantCase(featureName),
								constantGroupName,
								featureName);
		}
	}

	private static boolean isSingleMutable(final Feature<?, ?> feature)
	{
		return !feature.immutable() && !feature.many();
	}
}
