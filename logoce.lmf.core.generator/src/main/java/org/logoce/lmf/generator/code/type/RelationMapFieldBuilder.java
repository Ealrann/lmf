package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupBuilderClassType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.feature.MethodUtil;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.generator.group.builder.BuilderFeatureUtil;
import org.logoce.lmf.core.feature.RelationLazyInserter;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;

import javax.lang.model.element.Modifier;
import java.util.List;

public class RelationMapFieldBuilder implements CodeBuilder<List<FeatureResolution>, FieldSpec>
{
	private static final TypeParameter RELATION_MAP_CLASS = TypeParameter.of(RelationLazyInserter.class);
	private static final TypeParameter RELATION_MAP_BUILDER_CLASS = TypeParameter.of(RelationLazyInserter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};

	private final TypeParameter inserterType;
	private final TypeParameter inserterBuilderType;
	private final ClassName builderClassName;
	private final Group<?> ownerGroup;

	public RelationMapFieldBuilder(final Group<?> group)
	{
		this.ownerGroup = group;
		final var builderType = group.adapt(GroupBuilderClassType.class);
		builderClassName = builderType.raw();
		inserterType = RELATION_MAP_CLASS.nest(builderClassName);
		inserterBuilderType = RELATION_MAP_BUILDER_CLASS.nest(builderClassName);
	}

	@Override
	public FieldSpec build(final List<FeatureResolution> featureResolutions)
	{
		final var relations = featureResolutions.stream()
												.filter(RelationMapFieldBuilder::isRelation)
												.toList();

		final var featureCount = relations.size();

		final var statementBuilder = CodeBlock.builder();
		statementBuilder.add("new $T($L, Inserters::relationIndex)", inserterBuilderType.parametrized(), featureCount);

		relations.stream()
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
		final var interfaceType = ownerGroup.adapt(GroupInterfaceType.class);
		final var domainType = interfaceType.raw();

		final var methodName = MethodUtil.builderMethodName(resolution);
		final var usesRawSetter = BuilderFeatureUtil.needsRawSetter(resolution, ownerGroup);
		final var usedMethod = usesRawSetter ? '_' + methodName : methodName;
		final var paramType = usesRawSetter
							  ? BuilderFeatureUtil.rawSetterParameterType(resolution, ownerGroup)
							  : resolution.builderParameterSpec(ownerGroup).type;

		final var constantName = GenUtils.toConstantCase(resolution.name());

		return CodeBlock.of(".add($T.FeatureIDs.$N, (builder, value) -> builder.$N(($T) value))",
							domainType,
							constantName,
							usedMethod,
							paramType);
	}

	private static boolean isRelation(final FeatureResolution f)
	{
		return f.feature() instanceof Relation<?, ?, ?, ?>;
	}
}
