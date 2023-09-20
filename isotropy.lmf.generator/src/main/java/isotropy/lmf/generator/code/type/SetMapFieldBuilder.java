package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.feature.FeatureSetter;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.adapter.GroupInterfaceType;
import isotropy.lmf.generator.adapter.ModelResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

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
			final var model = (Model) ModelUtils.root(resolution.feature());
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
