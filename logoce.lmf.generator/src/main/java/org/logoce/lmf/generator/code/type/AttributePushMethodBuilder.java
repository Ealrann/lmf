package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeVariableName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.core.lang.Attribute;

import javax.lang.model.element.Modifier;
import java.util.List;

public final class AttributePushMethodBuilder implements CodeBuilder<List<FeatureResolution>, MethodSpec>
{
	private static final MethodSpec.Builder METHOD_BUILDER = prepareBuilder();

	@Override
	public MethodSpec build(final List<FeatureResolution> context)
	{
		return METHOD_BUILDER.build();
	}

	private static MethodSpec.Builder prepareBuilder()
	{
		final var typeVariable = TypeVariableName.get("AttributeType");
		final var paramAttribute = TypeParameter.of(ClassName.get(Attribute.class), 4);

		return MethodSpec.methodBuilder("push")
						 .addModifiers(Modifier.PUBLIC)
						 .addTypeVariable(typeVariable)
						 .addParameter(ParameterSpec.builder(paramAttribute.parametrized(), "attribute", Modifier.FINAL)
													.build())
						 .addParameter(ParameterSpec.builder(typeVariable, "value", Modifier.FINAL).build())
						 .addStatement("Inserters.ATTRIBUTE_INSERTER.push(this, attribute.id(), value)")
						 .addAnnotation(Override.class);
	}
}
