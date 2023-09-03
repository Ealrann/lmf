package isotropy.lmf.generator.group.feature;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class MethodBuilder implements CodeBuilder<MethodSpec>
{
	private final Modifier[] modifiers;
	private final Function<FeatureResolution, String> nameResolver;
	private final Function<FeatureResolution, TypeName> returnResolver;
	private final Optional<Function<FeatureResolution, ParameterSpec>> parameterResolver;
	private final Optional<Function<FeatureResolution, List<String>>> statementResolver;

	public MethodBuilder(Modifier[] modifiers,
						 Function<FeatureResolution, String> nameResolver,
						 Function<FeatureResolution, TypeName> returnResolver,
						 Optional<Function<FeatureResolution, ParameterSpec>> parameterResolver,
						 Optional<Function<FeatureResolution, List<String>>> statementResolver)
	{
		assert modifiers != null;

		this.modifiers = modifiers;
		this.nameResolver = nameResolver;
		this.returnResolver = returnResolver;
		this.parameterResolver = parameterResolver;
		this.statementResolver = statementResolver;
	}

	public MethodBuilder(Modifier[] modifiers,
						 Function<FeatureResolution, String> nameResolver,
						 Function<FeatureResolution, TypeName> returnResolver)
	{
		this(modifiers, nameResolver, returnResolver, Optional.empty(), Optional.empty());
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

		return spec.build();
	}
}
