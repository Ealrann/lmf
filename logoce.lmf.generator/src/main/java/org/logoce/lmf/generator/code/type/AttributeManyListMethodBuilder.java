package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.feature.MethodUtil;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;

public final class AttributeManyListMethodBuilder implements CodeBuilder<FeatureResolution, MethodSpec>
{
	private final TypeName returnType;
	private final Group<?> ownerGroup;

	public AttributeManyListMethodBuilder(final TypeName returnType, final Group<?> ownerGroup)
	{
		this.returnType = returnType;
		this.ownerGroup = ownerGroup;
	}

	@Override
	public MethodSpec build(final FeatureResolution resolution)
	{
		final var methodName = "add" + GenUtils.capitalizeFirstLetter(resolution.name());
		final var paramName = MethodUtil.validateParameterName(resolution.name());
		final var elementType = boxIfPrimitive(resolution.singleTypeFor(ownerGroup).parametrized());
		final var listType = ParameterizedTypeName.get(ConstantTypes.LIST, elementType.box());
		final var parameter = ParameterSpec.builder(listType, paramName, Modifier.FINAL).build();

		return MethodSpec.methodBuilder(methodName)
						 .addModifiers(Modifier.PUBLIC)
						 .addAnnotation(ConstantTypes.OVERRIDE)
						 .returns(returnType)
						 .addParameter(parameter)
						 .addStatement("this.$N.addAll($N)", resolution.name(), paramName)
						 .addStatement("return this")
						 .build();
	}

	public static boolean isManyAttribute(final FeatureResolution resolution)
	{
		return resolution.feature() instanceof Attribute<?, ?> attribute && attribute.many();
	}

	private static TypeName boxIfPrimitive(final TypeName type)
	{
		return type.isPrimitive() ? type.box() : type;
	}
}
