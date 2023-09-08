package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.RelationLazyInserter;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.feature.MethodUtil;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GroupType;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.List;

public class RelationMapFieldBuilder implements CodeBuilder<List<FeatureResolution>, FieldSpec>
{
	public static final ClassName RELATION_MAP_CLASS = ClassName.get(RelationLazyInserter.class);
	public static final ClassName RELATION_MAP_BUILDER_CLASS = ClassName.get(RelationLazyInserter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};
	private final TypeParameter inserterType;
	private final TypeParameter inserterBuilderType;
	private final ClassName interfaceClassName;
	private final ClassName builderClassName;

	public RelationMapFieldBuilder(final GroupType interfaceType, final GroupType builderType)
	{
		final var wildcardInterface = builderType.parametrizedWildcard();

		interfaceClassName = interfaceType.raw();
		builderClassName = builderType.raw();
		inserterType = TypeParameter.of(RELATION_MAP_CLASS, wildcardInterface);
		inserterBuilderType = TypeParameter.of(RELATION_MAP_BUILDER_CLASS, wildcardInterface);
	}

	@Override
	public FieldSpec build(final List<FeatureResolution> featureResolutions)
	{
		final var statementBuilder = CodeBlock.builder();
		statementBuilder.add("new $T()", inserterBuilderType.parametrized());

		featureResolutions.stream()
						  .filter(RelationMapFieldBuilder::isRelation)
						  .map(this::buildStatement)
						  .forEach(statementBuilder::add);

		statementBuilder.add(".build()");

		return FieldSpec.builder(inserterType.parametrized(), "RELATION_INSERTER")
						.addModifiers(modifiers)
						.initializer(statementBuilder.build())
						.build();
	}

	private CodeBlock buildStatement(final FeatureResolution resolution)
	{
		final var methodName = MethodUtil.builderMethodName(resolution);
		final var featureName = resolution.name();
		return CodeBlock.of(".add($T.Features.$N, $T::$N)",
							interfaceClassName,
							featureName,
							builderClassName,
							methodName);
	}

	private static boolean isRelation(final FeatureResolution f)
	{
		return f.feature() instanceof Relation<?, ?>;
	}
}
