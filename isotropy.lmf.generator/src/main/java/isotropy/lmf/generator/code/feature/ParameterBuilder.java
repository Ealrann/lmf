package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.function.Function;

public final class ParameterBuilder implements FeatureBuilder<ParameterSpec>
{
	private final Function<FeatureResolution, String> nameResolver;
	private final Function<FeatureResolution, TypeName> typeResolver;

	public ParameterBuilder(Function<FeatureResolution, String> nameResolver,
							Function<FeatureResolution, TypeName> typeResolver)
	{
		this.nameResolver = nameResolver;
		this.typeResolver = typeResolver;
	}

	@Override
	public ParameterSpec build(FeatureResolution resolution)
	{
		final var spec = ParameterSpec.builder(typeResolver.apply(resolution), nameResolver.apply(resolution))
									  .addModifiers(Modifier.FINAL)
									  .build();

		return spec;
	}
}
