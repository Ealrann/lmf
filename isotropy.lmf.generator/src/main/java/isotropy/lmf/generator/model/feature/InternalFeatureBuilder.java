package isotropy.lmf.generator.model.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.model.RawFeature;
import isotropy.lmf.core.util.ModelUtils;

import javax.lang.model.element.Modifier;

public class InternalFeatureBuilder
{
	public static final String INITIALIZER_PATTERN = "new RawFeature<>(%1$b,%2$b,%3$s)";
	private final Group<?> group;

	public InternalFeatureBuilder(Group<?> group)
	{
		this.group = group;
	}

	public FieldSpec toConstantFeature(FeatureResolution featureResolution)
	{
		final var feature = featureResolution.feature();
		final var singleType = featureResolution.singleType()
												.parametrizedWildcard();
		final var effectiveType = featureResolution.effectiveType()
												   .parametrizedWildcard();

		final var type = ParameterizedTypeName.get(ClassName.get(RawFeature.class),
												   singleType.box(),
												   effectiveType.box());

		final var initializer = feature.lmContainer() == group ? localInitializer(feature) : parentInitializer(feature);

		return FieldSpec.builder(type, feature.name())
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
						.initializer(initializer)
						.build();
	}

	private static String localInitializer(final Feature<?, ?> feature)
	{
		final var model = (Model) ModelUtils.root(feature);
		final var definitionFile = model.name() + "Definition";
		final var many = feature.many();
		final var relation = feature instanceof Relation<?, ?>;
		final var featureDefinition = String.format("() -> %1$s.Features.GROUP.includes.%2$s",
													definitionFile,
													feature.name());

		return INITIALIZER_PATTERN.formatted(many, relation, featureDefinition);
	}

	private static String parentInitializer(final Feature<?, ?> feature)
	{
		final var group = (Group<?>) feature.lmContainer();
		final var parentName = group.name();
		return parentName + ".Features." + feature.name();
	}
}
