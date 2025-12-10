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
import org.logoce.lmf.model.feature.FeatureInserter;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;
import java.util.List;

public class AttributeMapFieldBuilder implements CodeBuilder<List<FeatureResolution>, FieldSpec>
{
	private static final TypeParameter ATTRIBUTE_MAP_CLASS = TypeParameter.of(FeatureInserter.class);
	private static final TypeParameter ATTRIBUTE_MAP_BUILDER_CLASS = TypeParameter.of(FeatureInserter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};

	private final TypeParameter inserterType;
	private final TypeParameter inserterBuilderType;
	private final ClassName builderClassName;
	private final Group<?> ownerGroup;

	public AttributeMapFieldBuilder(final Group<?> group)
	{
		this.ownerGroup = group;
		final var builderType = group.adapt(GroupBuilderClassType.class);
		builderClassName = builderType.raw();
		inserterType = ATTRIBUTE_MAP_CLASS.nest(builderClassName);
		inserterBuilderType = ATTRIBUTE_MAP_BUILDER_CLASS.nest(builderClassName);
	}

	@Override
	public FieldSpec build(final List<FeatureResolution> featureResolutions)
	{
		final var attributes = featureResolutions.stream()
												.filter(AttributeMapFieldBuilder::isAttribute)
												.toList();

		final var featureCount = attributes.size();

		final var statementBuilder = CodeBlock.builder();
		statementBuilder.add("new $T($L, Inserters::attributeIndex)", inserterBuilderType.parametrized(), featureCount);

		attributes.stream()
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

	private static boolean isAttribute(final FeatureResolution f)
	{
		return f.feature() instanceof Attribute<?, ?>;
	}
}
