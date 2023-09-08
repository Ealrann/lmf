package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.generator.util.ConstantTypes;

import javax.lang.model.element.Modifier;
import java.util.function.Function;

public final class FeatureFieldBuilder implements FeatureBuilder<FieldSpec>
{
	private final Modifier[] FINAL_MODIFIERS = new Modifier[]{Modifier.PRIVATE, Modifier.FINAL};
	private final Modifier[] MODIFIERS = new Modifier[]{Modifier.PRIVATE};
	private final boolean forBuilder;
	private final Function<FeatureResolution, String> nameResolver;
	private final Function<FeatureResolution, TypeName> typeResolver;

	public FeatureFieldBuilder(final boolean forBuilder,
							   final Function<FeatureResolution, String> nameResolver,
							   final Function<FeatureResolution, TypeName> typeResolver)
	{
		this.forBuilder = forBuilder;
		this.nameResolver = nameResolver;
		this.typeResolver = typeResolver;
	}

	@Override
	public FieldSpec build(FeatureResolution resolution)
	{
		final var feature = resolution.feature();
		final var many = feature.many();
		final var isFinal = feature.immutable() && !forBuilder;
		final var modifiers = isFinal || many ? FINAL_MODIFIERS : MODIFIERS;
		final var typeName = typeResolver.apply(resolution);
		final var name = nameResolver.apply(resolution);

		final var spec = FieldSpec.builder(typeName, name).addModifiers(modifiers);

		if (many && (forBuilder || !isFinal))
		{
			spec.initializer("new $T<>()", ConstantTypes.ARRAYLIST);
		}

		return spec.build();
	}
}
