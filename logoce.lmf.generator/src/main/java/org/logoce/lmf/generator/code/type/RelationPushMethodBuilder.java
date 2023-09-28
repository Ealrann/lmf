package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.lang.Relation;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Supplier;

public class RelationPushMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	private static final MethodSpec.Builder METHOD_BUILDER = prepareBuilder();

	@Override
	public MethodSpec build(final List<FeatureResolution> context)
	{
		return METHOD_BUILDER.build();
	}

	private static MethodSpec.Builder prepareBuilder()
	{
		final var attributeType = ClassName.get(Relation.class);
		final var variableName = TypeVariableName.get("RelationType", ConstantTypes.LM_OBJECT);
		final var supplierType = ClassName.get(Supplier.class);
		final var paramAttribute = TypeParameter.of(attributeType, List.of(variableName, GenUtils.WILDCARD));
		final var suppliedType = TypeParameter.of(supplierType, variableName);
		final var featureRes = GenUtils.USE_RAWFEATURE_FOR_MODEL ? "relation.rawFeature()" : "relation";

		return MethodSpec.methodBuilder("push")
						 .addModifiers(Modifier.PUBLIC)
						 .addTypeVariable(variableName)
						 .addParameter(ParameterSpec.builder(paramAttribute.parametrized(), "relation", Modifier.FINAL)
													.build())
						 .addParameter(ParameterSpec.builder(suppliedType.parametrized(), "supplier", Modifier.FINAL)
													.build())
						 .addStatement("RELATION_INSERTER.push(this, $N, supplier)", featureRes)
						 .addAnnotation(Override.class);
	}
}
