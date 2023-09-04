package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.function.Function;

public final class FieldBuilder implements FeatureBuilder<FieldSpec>
{
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.FINAL};

	private final Function<FeatureResolution, String> nameResolver;
	private final Function<FeatureResolution, TypeName> typeResolver;

	public FieldBuilder(Function<FeatureResolution, String> nameResolver,
						Function<FeatureResolution, TypeName> typeResolver)
	{
		this.nameResolver = nameResolver;
		this.typeResolver = typeResolver;
	}

	@Override
	public FieldSpec build(FeatureResolution resolution)
	{
		final var spec = FieldSpec.builder(typeResolver.apply(resolution), nameResolver.apply(resolution))
								  .addModifiers(modifiers)
								  .build();

		return spec;
	}
}
