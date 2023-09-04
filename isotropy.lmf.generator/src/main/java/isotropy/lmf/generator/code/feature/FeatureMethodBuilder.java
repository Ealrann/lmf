package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class FeatureMethodBuilder implements FeatureBuilder<MethodSpec>
{
	private final Modifier[] modifiers;
	private final Function<FeatureResolution, String> nameResolver;
	private final Function<FeatureResolution, TypeName> returnResolver;
	private final Optional<Function<FeatureResolution, ParameterSpec>> parameterResolver;
	private final Optional<Function<FeatureResolution, List<String>>> statementResolver;
	private final boolean overide;

	public FeatureMethodBuilder(final Modifier[] modifiers,
								final Function<FeatureResolution, String> nameResolver,
								final Function<FeatureResolution, TypeName> returnResolver,
								final Optional<Function<FeatureResolution, ParameterSpec>> parameterResolver,
								final Optional<Function<FeatureResolution, List<String>>> statementResolver,
								final boolean overide)
	{
		assert modifiers != null;

		this.modifiers = modifiers;
		this.nameResolver = nameResolver;
		this.returnResolver = returnResolver;
		this.parameterResolver = parameterResolver;
		this.statementResolver = statementResolver;
		this.overide = overide;
	}

	public FeatureMethodBuilder(final Modifier[] modifiers,
								final Function<FeatureResolution, String> nameResolver,
								final Function<FeatureResolution, TypeName> returnResolver)
	{
		this(modifiers, nameResolver, returnResolver, Optional.empty(), Optional.empty(), false);
	}

	@Override
	public MethodSpec build(FeatureResolution resolution)
	{
		final var spec = MethodSpec.methodBuilder(nameResolver.apply(resolution))
								   .addModifiers(modifiers)
								   .returns(returnResolver.apply(resolution));

		parameterResolver.ifPresent(f -> spec.addParameter(f.apply(resolution)));

		statementResolver.ifPresent(r -> r.apply(resolution)
										  .forEach(spec::addStatement));

		if (overide) spec.addAnnotation(Override.class);

		return spec.build();
	}
}
