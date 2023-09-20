package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.feature.FeatureInserter;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.adapter.FeatureResolution;
import isotropy.lmf.generator.adapter.GroupBuilderClassType;
import isotropy.lmf.generator.adapter.GroupInterfaceType;
import isotropy.lmf.generator.adapter.ModelResolution;
import isotropy.lmf.generator.code.feature.MethodUtil;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.List;

public class AttributeMapFieldBuilder implements CodeBuilder<List<FeatureResolution>, FieldSpec>
{
	private static final TypeParameter ATTRIBUTE_MAP_CLASS = TypeParameter.of(FeatureInserter.class);
	private static final TypeParameter ATTRIBUTE_MAP_BUILDER_CLASS = TypeParameter.of(FeatureInserter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};

	private final TypeParameter inserterType;
	private final TypeParameter inserterBuilderType;
	private final ClassName interfaceClassName;
	private final ClassName builderClassName;

	public AttributeMapFieldBuilder(final Group<?> group)
	{
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var builderType = group.adapt(GroupBuilderClassType.class);
		final var wildcardBuilder = builderType.parametrizedWildcard();

		interfaceClassName = interfaceType.raw();
		builderClassName = builderType.raw();
		inserterType = ATTRIBUTE_MAP_CLASS.nest(wildcardBuilder);
		inserterBuilderType = ATTRIBUTE_MAP_BUILDER_CLASS.nest(wildcardBuilder);
	}

	@Override
	public FieldSpec build(final List<FeatureResolution> featureResolutions)
	{
		final var statementBuilder = CodeBlock.builder();
		statementBuilder.add("new $T()", inserterBuilderType.parametrized());

		featureResolutions.stream()
						  .filter(AttributeMapFieldBuilder::isAttribute)
						  .map(this::buildStatement)
						  .forEach(statementBuilder::add);

		statementBuilder.add(".build()");

		return FieldSpec.builder(inserterType.parametrized(), "ATTRIBUTE_INSERTER")
						.addModifiers(modifiers)
						.initializer(statementBuilder.build())
						.build();
	}

	private CodeBlock buildStatement(final FeatureResolution resolution)
	{
		final var hasGenerics = resolution.hasGeneric();
		final var methodName = MethodUtil.builderMethodName(resolution);
		final var usedMethod = hasGenerics ? '_' + methodName : methodName;
		final var group = (Group<?>) resolution.feature().lmContainer();
		final var featureName = resolution.name();

		if (GenUtils.USE_RAWFEATURE_FOR_MODEL)
		{
			return CodeBlock.of(".add($T.Features.$N, $T::$N)",
								interfaceClassName,
								featureName,
								builderClassName,
								usedMethod);
		}
		else
		{
			final var model = (Model) ModelUtils.root(resolution.feature());
			final var modelDefinition = model.adapt(ModelResolution.class).modelDefinition;
			final var constantGroupName = GenUtils.toConstantCase(group.name());
			return CodeBlock.of(".add($T.Features.$N.$N, $T::$N)",
								modelDefinition,
								constantGroupName,
								GenUtils.toConstantCase(featureName),
								builderClassName,
								usedMethod);
		}
	}

	private static boolean isAttribute(final FeatureResolution f)
	{
		return f.feature() instanceof Attribute<?, ?>;
	}
}
