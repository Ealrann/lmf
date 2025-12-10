package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupImplementationType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.feature.FeatureGetter;
import org.logoce.lmf.model.lang.Group;

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
		final var implementationType = group.adapt(GroupImplementationType.class);

		final var features = FeatureStreams.distinctFeatures(group).toList();
		final var featureCount = features.size();

		final var initializerBuilder = CodeBlock.builder()
												.add("new $T($L, $T::featureIndexStatic)", builderType.parametrized(), featureCount, implementationType.raw());

		features.stream()
				.map(f -> f.adapt(FeatureResolution.class))
				.map(this::buildStatement)
				.forEach(initializerBuilder::add);
		initializerBuilder.add(".build()");

		return FieldSpec.builder(type.parametrized(), "GET_MAP")
						.addModifiers(modifiers)
						.initializer(initializerBuilder.build())
						.build();
	}

	private CodeBlock buildStatement(final FeatureResolution resolution)
	{
		final var featureName = resolution.name();
		final var constantName = GenUtils.toConstantCase(featureName);
		final var domainType = interfaceType.raw();

		return CodeBlock.of(".add($T.FeatureIDs.$N, $T::$N)", domainType, constantName, interfaceType.raw(), featureName);
	}
}
