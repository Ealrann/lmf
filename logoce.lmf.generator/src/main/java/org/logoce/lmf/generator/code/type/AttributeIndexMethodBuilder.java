package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;
import java.util.List;

public final class AttributeIndexMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	private final Group<?> group;

	public AttributeIndexMethodBuilder(final Group<?> group)
	{
		this.group = group;
	}

	@Override
	public MethodSpec build(final List<FeatureResolution> featureResolutions)
	{
		final var interfaceType = group.adapt(GroupInterfaceType.class);
		final var domainType = interfaceType.raw();

		final var attributes = featureResolutions.stream()
												 .map(FeatureResolution::feature)
												 .filter(f -> f instanceof Attribute<?, ?>)
												 .toList();

		final var methodBuilder = MethodSpec.methodBuilder("attributeIndex")
											.addModifiers(Modifier.PRIVATE, Modifier.STATIC)
											.returns(int.class)
											.addParameter(int.class, "featureId", Modifier.FINAL);

		if (attributes.isEmpty())
		{
			methodBuilder.addStatement("throw new $T($S + featureId)",
									   IllegalArgumentException.class,
									   "Unknown attribute featureId: ");
		}
		else
		{
			final var body = CodeBlock.builder();
			body.add("return switch (featureId) {\n");

			int index = 0;
			for (final var feature : attributes)
			{
				final var constantName = GenUtils.toConstantCase(feature.name());
				body.addStatement("  case $T.FeatureIDs.$N -> $L", domainType, constantName, index++);
			}

			body.addStatement("  default -> throw new $T($S + featureId)",
							  IllegalArgumentException.class,
							  "Unknown attribute featureId: ");
			body.add("};\n");

			methodBuilder.addCode(body.build());
		}

		return methodBuilder.build();
	}
}
