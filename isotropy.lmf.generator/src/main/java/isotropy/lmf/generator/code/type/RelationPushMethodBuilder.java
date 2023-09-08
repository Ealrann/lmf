package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Supplier;

public class RelationPushMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	@Override
	public MethodSpec build(final List<FeatureResolution> context)
	{
		final var attributeType = ClassName.get(Relation.class);
		final var variableName = TypeVariableName.get("RelationType", ClassName.get(LMObject.class));
		final var supplierType = ClassName.get(Supplier.class);
		final var paramAttribute = TypeParameter.of(attributeType, List.of(variableName, GenUtils.WILDCARD));
		final var suppliedType = TypeParameter.of(supplierType, variableName);

		return MethodSpec.methodBuilder("push")
						 .addModifiers(Modifier.PUBLIC)
						 .addTypeVariable(variableName)
						 .addParameter(ParameterSpec.builder(paramAttribute.parametrized(), "relation", Modifier.FINAL)
													.build())
						 .addParameter(ParameterSpec.builder(suppliedType.parametrized(), "supplier", Modifier.FINAL)
													.build())
						 .addStatement("RELATION_INSERTER.push(this, relation.rawFeature(), supplier)")
						 .addAnnotation(Override.class)
						 .build();
	}
}
