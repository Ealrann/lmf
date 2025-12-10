package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Builds the static holder class that contains the ATTRIBUTE_INSERTER and
 * RELATION_INSERTER fields for a generated builder class. This mirrors the
 * Inserters pattern used in implementation classes and avoids early static
 * initialization of feature maps.
 */
public final class BuilderInserterMapHolderBuilder implements CodeBuilder<List<FeatureResolution>, TypeSpec>
{
	private final AttributeMapFieldBuilder attributeMapFieldBuilder;
	private final RelationMapFieldBuilder relationMapFieldBuilder;

	public BuilderInserterMapHolderBuilder(final Group<?> group)
	{
		this.attributeMapFieldBuilder = new AttributeMapFieldBuilder(group);
		this.relationMapFieldBuilder = new RelationMapFieldBuilder(group);
	}

	@Override
	public TypeSpec build(final List<FeatureResolution> featureResolutions)
	{
		return TypeSpec.classBuilder("Inserters")
					   .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
					   .addField(attributeMapFieldBuilder.build(featureResolutions))
					   .addField(relationMapFieldBuilder.build(featureResolutions))
					   .build();
	}
}

