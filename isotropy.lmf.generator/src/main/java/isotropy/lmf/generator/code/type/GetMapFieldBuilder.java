package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.feature.FeatureGetter;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.adapter.GroupInterfaceType;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class GetMapFieldBuilder implements CodeBuilder<Group<?>, FieldSpec>
{
	public static final ClassName GETTER_MAP_CLASS = ClassName.get(FeatureGetter.class);
	public static final ClassName GETTER_MAP_BUILDER_CLASS = ClassName.get(FeatureGetter.Builder.class);
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
		final var type = TypeParameter.of(GETTER_MAP_CLASS, wildcardInterface);
		final var builderType = TypeParameter.of(GETTER_MAP_BUILDER_CLASS, wildcardInterface);
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
		final var group = (Group<?>) resolution.feature().lmContainer();
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
			final var model = (Model) ModelUtils.root(resolution.feature());
			final var modelDefinition = ClassName.get(model.domain(), model.name() + "Definition");
			return CodeBlock.of(".add($T.Features.$N.$N, $T::$N)",
								modelDefinition,
								constantGroupName,
								GenUtils.toConstantCase(featureName),
								interfaceType.raw(),
								featureName);
		}
	}
}
