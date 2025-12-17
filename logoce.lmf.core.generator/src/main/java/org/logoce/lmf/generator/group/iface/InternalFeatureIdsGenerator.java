package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.code.model.FeatureIdFieldBuilder;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.core.lang.Group;

import javax.lang.model.element.Modifier;

public final class InternalFeatureIdsGenerator
{
	private final Group<?> group;

	public InternalFeatureIdsGenerator(final Group<?> group)
	{
		this.group = group;
	}

	public TypeSpec build()
	{
		final var featureIdsInterfaceBuilder = TypeSpec.interfaceBuilder("FeatureIDs")
													   .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		final var fieldBuilder = new FeatureIdFieldBuilder(group);

		FeatureStreams.distinctFeatures(group)
					  .map(fieldBuilder::build)
					  .forEach(featureIdsInterfaceBuilder::addField);

		return featureIdsInterfaceBuilder.build();
	}
}
