package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.code.model.FeaturesFieldBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;
import java.util.List;

public final class GroupFeaturesGenerator
{
	private final Group<?> group;

	public GroupFeaturesGenerator(final Group<?> group)
	{
		this.group = group;
	}

	public TypeSpec build()
	{
		final var featuresInterfaceBuilder = TypeSpec.interfaceBuilder("Features")
													.addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		final var fieldBuilder = new FeaturesFieldBuilder(group);

		final List<FieldSpec> fields = FeatureStreams.distinctFeatures(group)
													 .map(fieldBuilder::build)
													 .toList();

		fields.forEach(featuresInterfaceBuilder::addField);

		final var allType = TypeParameter.of(ConstantTypes.LIST, ConstantTypes.FEATURE.parametrizedWildcard());
		final var listBlock = CodeBlock.builder().add("$T.of(", ConstantTypes.LIST);
		boolean first = true;
		for (final var field : fields)
		{
			if (first) first = false;
			else listBlock.add(", ");
			listBlock.add("$N", field);
		}
		listBlock.add(")");

		featuresInterfaceBuilder.addField(FieldSpec.builder(allType.parametrized(), "ALL",
															Modifier.PUBLIC,
															Modifier.STATIC,
															Modifier.FINAL)
												   .initializer(listBlock.build())
												   .build());

		return featuresInterfaceBuilder.build();
	}
}

