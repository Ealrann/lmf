package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.code.model.FeaturesFieldBuilder;
import org.logoce.lmf.generator.util.FeatureStreams;
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

		return featuresInterfaceBuilder.build();
	}
}
