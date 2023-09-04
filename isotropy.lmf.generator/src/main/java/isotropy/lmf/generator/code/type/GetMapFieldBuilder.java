package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import isotropy.lmf.core.model.FeatureGetter;
import isotropy.lmf.generator.code.CodeBuilder;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;

public class GetMapFieldBuilder implements CodeBuilder<TypeFeatures, FieldSpec>
{
	public static final ClassName GETTER_MAP_CLASS = ClassName.get(FeatureGetter.class);
	public static final ClassName GETTER_MAP_BUILDER_CLASS = ClassName.get(FeatureGetter.Builder.class);
	private static final Modifier[] modifiers = new Modifier[]{Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL};

	@Override
	public FieldSpec build(final TypeFeatures context)
	{
		final var typedInterface = context.interfaceType();
		final var type = TypeParameter.of(GETTER_MAP_CLASS, typedInterface.parametrizedWildcard());
		final var builderType = TypeParameter.of(GETTER_MAP_BUILDER_CLASS, typedInterface.parametrizedWildcard());
		final var groupName = context.group()
								   .name();
		final var statementBuilder = new StringBuilder();
		statementBuilder.append("new $T()");
		for (final var feature : context.resolutions())
		{
			final var fname = feature.name();
			statementBuilder.append(String.format(".add(Features.%1$s, %2$s::%1$s)", fname, groupName));
		}
		statementBuilder.append(".build()");

		final var spec = FieldSpec.builder(type.parametrized(), "GET_MAP")
								  .addModifiers(modifiers)
								  .initializer(statementBuilder.toString(), builderType.parametrized())
								  .build();

		return spec;
	}

}
