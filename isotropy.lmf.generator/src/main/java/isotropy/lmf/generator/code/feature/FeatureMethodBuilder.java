package isotropy.lmf.generator.code.feature;

import com.squareup.javapoet.*;

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
	private final Optional<Function<FeatureParameter, List<CodeBlock>>> statementResolver;
	private final List<AnnotationSpec> annotations;

	public FeatureMethodBuilder(final Modifier[] modifiers,
								final Function<FeatureResolution, String> nameResolver,
								final Function<FeatureResolution, TypeName> returnResolver,
								final Optional<Function<FeatureResolution, ParameterSpec>> parameterResolver,
								final Optional<Function<FeatureParameter, List<CodeBlock>>> statementResolver,
								final List<AnnotationSpec> annotations)
	{
		assert modifiers != null;

		this.modifiers = modifiers;
		this.nameResolver = nameResolver;
		this.returnResolver = returnResolver;
		this.parameterResolver = parameterResolver;
		this.statementResolver = statementResolver;
		this.annotations = annotations;
	}

	public FeatureMethodBuilder(final Modifier[] modifiers,
								final Function<FeatureResolution, String> nameResolver,
								final Function<FeatureResolution, TypeName> returnResolver)
	{
		this(modifiers, nameResolver, returnResolver, Optional.empty(), Optional.empty(), List.of());
	}

	@Override
	public MethodSpec build(FeatureResolution resolution)
	{
		final var spec = MethodSpec.methodBuilder(nameResolver.apply(resolution))
								   .addModifiers(modifiers)
								   .returns(returnResolver.apply(resolution));

		final var builtParameter = buildParameter(resolution, spec);

		statementResolver.ifPresent(r -> r.apply(builtParameter).forEach(spec::addStatement));

		spec.addAnnotations(annotations);

		return spec.build();
	}

	private FeatureParameter buildParameter(final FeatureResolution resolution, final MethodSpec.Builder spec)
	{
		if (parameterResolver.isPresent())
		{
			final var resolver = parameterResolver.get();
			final var parameterSpec = resolver.apply(resolution);
			spec.addParameter(parameterSpec);
			return new FeatureParameter(resolution, parameterSpec);
		}
		else
		{
			return new FeatureParameter(resolution, null);
		}
	}
}
