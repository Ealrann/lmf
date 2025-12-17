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
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;

import javax.lang.model.element.Modifier;

public final class RelationManyListMethodBuilder implements CodeBuilder<FeatureResolution, MethodSpec>
{
	private final TypeName returnType;
	private final Group<?> ownerGroup;

	public RelationManyListMethodBuilder(final TypeName returnType, final Group<?> ownerGroup)
	{
		this.returnType = returnType;
		this.ownerGroup = ownerGroup;
	}

	@Override
	public MethodSpec build(final FeatureResolution resolution)
	{
		final var methodName = "add" + GenUtils.capitalizeFirstLetter(resolution.name());
		final var paramName = MethodUtil.validateParameterName(resolution.name());
		final var elementType = resolution.singleTypeFor(ownerGroup).parametrizedWildcard();
		final var listType = ParameterizedTypeName.get(ConstantTypes.LIST, elementType.box());
		final var supplierType = ParameterizedTypeName.get(ConstantTypes.SUPPLIER, elementType.box());

		final var parameter = ParameterSpec.builder(listType, paramName, Modifier.FINAL).build();

		return MethodSpec.methodBuilder(methodName)
						 .addModifiers(Modifier.PUBLIC)
						 .addAnnotation(ConstantTypes.OVERRIDE)
						 .returns(returnType)
						 .addParameter(parameter)
						 .addStatement("$N.stream().map(value -> ($T) () -> value).forEach(this.$N::add)",
									   paramName,
									   supplierType,
									   resolution.name())
						 .addStatement("return this")
						 .build();
	}

	public static boolean isManyRelation(final FeatureResolution resolution)
	{
		return resolution.feature() instanceof Relation<?, ?, ?, ?> relation && relation.many();
	}
}
