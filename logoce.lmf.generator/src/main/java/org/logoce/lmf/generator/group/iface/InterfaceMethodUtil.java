package org.logoce.lmf.generator.group.iface;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.feature.MethodUtil;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class InterfaceMethodUtil
{
	private static final Modifier[] METHOD_MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};
	private static final Modifier[] BUILDER_METHOD_MODIFIERS = {Modifier.ABSTRACT, Modifier.PUBLIC};

	public static FeatureMethodBuilder methodBuilder()
	{
		return new FeatureMethodBuilder(METHOD_MODIFIERS,
										FeatureResolution::name,
										InterfaceMethodUtil::interfaceReturnType);
	}

	public static FeatureMethodBuilder setterMethodBuilder()
	{
		return new FeatureMethodBuilder(METHOD_MODIFIERS,
										FeatureResolution::name,
										f -> TypeName.VOID,
										Optional.of(FeatureResolution::parameterSpec),
										Optional.empty(),
										List.of());
	}

	public static FeatureMethodBuilder builderMethodBuilder(TypeName typedBuilder, Group<?> owner)
	{
		return new FeatureMethodBuilder(BUILDER_METHOD_MODIFIERS,
										MethodUtil::builderMethodName,
										f -> typedBuilder,
										Optional.of(f -> f.builderParameterSpec(owner)),
										Optional.empty(),
										List.of());
	}

	public static FeatureMethodBuilder builderManyRelationListMethodBuilder(TypeName typedBuilder, Group<?> owner)
	{
		return new FeatureMethodBuilder(BUILDER_METHOD_MODIFIERS,
									   f -> "add" + GenUtils.capitalizeFirstLetter(f.name()),
									   f -> typedBuilder,
									   Optional.of(f ->
											   {
												   final var elementType = f.singleTypeFor(owner).parametrizedWildcard();
												   final var listType = ParameterizedTypeName.get(ConstantTypes.LIST,
																								   elementType.box());
												   final var paramName = MethodUtil.validateParameterName(f.name());
												   return ParameterSpec.builder(listType, paramName).build();
											   }),
									   Optional.empty(),
									   List.of());
	}

	public static FeatureMethodBuilder builderManyAttributeListMethodBuilder(TypeName typedBuilder, Group<?> owner)
	{
		return new FeatureMethodBuilder(BUILDER_METHOD_MODIFIERS,
									   f -> "add" + GenUtils.capitalizeFirstLetter(f.name()),
									   f -> typedBuilder,
									   Optional.of(f ->
											   {
												   final var elementType = f.singleTypeFor(owner).parametrized();
												   final var paramType = elementType.isPrimitive()
														   ? elementType.box()
														   : elementType;
												   final var listType = ParameterizedTypeName.get(ConstantTypes.LIST,
																								   paramType.box());
												   final var paramName = MethodUtil.validateParameterName(f.name());
												   return ParameterSpec.builder(listType, paramName).build();
											   }),
									   Optional.empty(),
									   List.of());
	}

	public static boolean isMutableSingle(final FeatureResolution resolution)
	{
		final Feature<?, ?, ?, ?> feature = resolution.feature();
		return !feature.many() && !feature.immutable();
	}

	private static TypeName interfaceReturnType(FeatureResolution resolution)
	{
		return resolution.effectiveType().parametrized();
	}
}
