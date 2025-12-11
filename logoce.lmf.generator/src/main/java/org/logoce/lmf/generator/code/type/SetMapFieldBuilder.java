package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupImplementationType;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.feature.FeatureSetter;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;

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

		final var features = FeatureStreams.distinctFeatures(group).toList();

		final var initializerBuilder = CodeBlock.builder()
												.add("new $T(FEATURE_COUNT, $T::featureIndexStatic)",
													 builderType.parametrized(),
													 implementationType.raw());

		features.stream()
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
		final TypeName valueType = resolution.effectiveTypeFor(ownerGroup).parametrized();

		final TypeName castType;
		if (valueType instanceof ParameterizedTypeName parameterized)
		{
			final var hasTypeVariable = parameterized.typeArguments.stream()
																   .anyMatch(TypeVariableName.class::isInstance);
			castType = hasTypeVariable ? parameterized.rawType : valueType;
		}
		else if (valueType instanceof TypeVariableName)
		{
			castType = TypeName.OBJECT;
		}
		else
		{
			castType = valueType;
		}

		final var constantName = GenUtils.toConstantCase(featureName);
		final var domainType = interfaceType.raw();

		return CodeBlock.of(".add($T.FeatureIDs.$N, (object, value) -> (($T) object).$N(($T) value))",
							domainType,
							constantName,
							implementationType.raw(),
							featureName,
							castType);
	}

	private static boolean isSingleMutable(final Feature<?, ?, ?, ?> feature)
	{
		return !feature.immutable() && !feature.many();
	}
}
