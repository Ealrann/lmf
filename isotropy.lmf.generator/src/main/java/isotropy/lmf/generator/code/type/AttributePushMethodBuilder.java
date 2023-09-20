package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeVariableName;
import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import javax.lang.model.element.Modifier;
import java.util.List;

public class AttributePushMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	@Override
	public MethodSpec build(final List<FeatureResolution> context)
	{
		final var attributeType = ClassName.get(Attribute.class);
		final var typeVariable = TypeVariableName.get("AttributeType");
		final var paramAttribute = TypeParameter.of(attributeType, List.of(typeVariable, GenUtils.WILDCARD));
		final var featureRes = GenUtils.USE_RAWFEATURE_FOR_MODEL ? "attribute.rawFeature()" : "attribute";

		return MethodSpec.methodBuilder("push")
						 .addModifiers(Modifier.PUBLIC)
						 .addTypeVariable(typeVariable)
						 .addParameter(ParameterSpec.builder(paramAttribute.parametrized(), "attribute", Modifier.FINAL)
													.build())
						 .addParameter(ParameterSpec.builder(typeVariable, "value", Modifier.FINAL).build())
						 .addStatement("ATTRIBUTE_INSERTER.push(this, $N, value)", featureRes)
						 .addAnnotation(Override.class)
						 .build();
	}
}
