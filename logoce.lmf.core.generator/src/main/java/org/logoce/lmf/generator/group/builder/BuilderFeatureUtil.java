package org.logoce.lmf.generator.group.builder;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.feature.FeatureFieldBuilder;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.feature.FeatureParameter;
import org.logoce.lmf.generator.code.feature.MethodUtil;
import org.logoce.lmf.generator.code.type.*;
import org.logoce.lmf.generator.code.util.CodeInstaller;
import org.logoce.lmf.generator.code.type.RelationManyListMethodBuilder;
import org.logoce.lmf.generator.code.type.AttributeManyListMethodBuilder;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.DefaultValueUtil;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.Relation;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class BuilderFeatureUtil
{
	private static final Modifier[] PUBLIC_ONLY = {Modifier.PUBLIC};
	public static final Modifier[] PRIVATE_ONLY = {Modifier.PRIVATE};
	public static final AttributePushMethodBuilder ATTRIBUTE_PUSH_BUILDER = new AttributePushMethodBuilder();
	public static final RelationPushMethodBuilder RELATION_PUSH_BUILDER = new RelationPushMethodBuilder();

	public static CodeInstaller<FeatureResolution> buildFeatureInstallers(final TypeSpec.Builder classBuilder,
																		  final TypeName builderType,
																		  final Group<?> ownerGroup)
	{
		final var setterBuilder = BuilderFeatureUtil.setterBuilder(builderType, ownerGroup);
		final var rawSetterBuilder = BuilderFeatureUtil.rawSetterBuilder(builderType, ownerGroup);
		final var relationManyListBuilder = new RelationManyListMethodBuilder(builderType, ownerGroup);
		final var attributeManyListBuilder = new AttributeManyListMethodBuilder(builderType, ownerGroup);
		final var fieldBuilder = BuilderFeatureUtil.fieldBuilder(ownerGroup);

		return CodeInstaller.compose(CodeInstaller.of(setterBuilder, classBuilder::addMethod),
									 CodeInstaller.of(fieldBuilder, classBuilder::addField),
									 CodeInstaller.of(rawSetterBuilder,
													  classBuilder::addMethod,
													  f -> needsRawSetter(f, ownerGroup)),
									 CodeInstaller.of(relationManyListBuilder,
													  classBuilder::addMethod,
													  RelationManyListMethodBuilder::isManyRelation),
									 CodeInstaller.of(attributeManyListBuilder,
													  classBuilder::addMethod,
													  AttributeManyListMethodBuilder::isManyAttribute));
	}

	public static CodeInstaller<List<FeatureResolution>> buildTypeInstallers(final TypeSpec.Builder classBuilder,
																			 final Group<?> group)
	{
		final var inserterMapHolderBuilder = new BuilderInserterMapHolderBuilder(group);
		final var buildMethodBuilder = new BuildMethodBuilder(group);

		return CodeInstaller.compose(CodeInstaller.of(buildMethodBuilder, classBuilder::addMethod),
				CodeInstaller.of(ATTRIBUTE_PUSH_BUILDER, classBuilder::addMethod),
				CodeInstaller.of(RELATION_PUSH_BUILDER, classBuilder::addMethod),
				CodeInstaller.of(inserterMapHolderBuilder, classBuilder::addType));
	}

	private static FeatureFieldBuilder fieldBuilder(final Group<?> ownerGroup)
	{
		return new FeatureFieldBuilder(true,
									   FeatureResolution::name,
									   f -> f.builderTypeFor(ownerGroup),
									   BuilderFeatureUtil::fieldInitializer);
	}

	private static Optional<CodeBlock> fieldInitializer(FeatureResolution resolution)
	{
		final var feature = resolution.feature();
		final var many = feature.many();

		if (many)
		{
			return Optional.of(CodeBlock.of("new $T<>()", ConstantTypes.ARRAYLIST));
		}
		else if (feature instanceof Relation<?, ?, ?, ?>)
		{
			return Optional.of(CodeBlock.of("() -> null"));
		}
		else if (feature instanceof Attribute<?, ?, ?, ?>)
		{
			return DefaultValueUtil.resolveDefaultValue(resolution);
		}
		else
		{
			return Optional.empty();
		}
	}

	private static FeatureMethodBuilder setterBuilder(TypeName returnType, Group<?> ownerGroup)
	{
		return new FeatureMethodBuilder(PUBLIC_ONLY,
										MethodUtil::builderMethodName,
										f -> returnType,
										Optional.of(f -> f.builderParameterSpec(ownerGroup)),
										Optional.of(p -> featureChangeStatement(p, false, ownerGroup)),
										List.of(ConstantTypes.OVERRIDE));
	}

	private static FeatureMethodBuilder rawSetterBuilder(TypeName returnType, Group<?> ownerGroup)
	{
		return new FeatureMethodBuilder(PRIVATE_ONLY,
										f -> '_' + f.name(),
										f -> returnType,
										Optional.of(f ->
												{
													final var paramType = rawSetterParameterType(f, ownerGroup);
													return ParameterSpec.builder(paramType,
																				 MethodUtil.builderSingleParameterName(f),
																				 Modifier.FINAL)
																		.build();
												}),
										Optional.of(p -> featureChangeStatement(p, true, ownerGroup)),
										List.of(ConstantTypes.SUPPRESS_RAW_UNCHECKED));
	}

	public static boolean needsRawSetter(final FeatureResolution resolution, final Group<?> ownerGroup)
	{
		if (!resolution.hasGeneric())
		{
			return false;
		}

		final var normalParamType = resolution.builderParameterSpec(ownerGroup).type;
		final var rawParamType = rawSetterParameterType(resolution, ownerGroup);
		return !normalParamType.equals(rawParamType);
	}

	public static TypeName rawSetterParameterType(final FeatureResolution resolution, final Group<?> ownerGroup)
	{
		final var baseType = resolution.rawSingleTypeFor(ownerGroup);
		final var feature = resolution.feature();
		final var isRelation = feature instanceof Relation<?, ?, ?, ?>;

		if (isRelation)
		{
			return ParameterizedTypeName.get(ConstantTypes.SUPPLIER, baseType.parametrizedWildcard().box());
		}
		else
		{
			return baseType.parametrizedWildcard();
		}
	}

	private static List<CodeBlock> featureChangeStatement(final FeatureParameter parameter,
														  final boolean raw,
														  final Group<?> ownerGroup)
	{
		return List.of(assignationStatement(parameter, raw, ownerGroup), CodeBlock.of("return this"));
	}

	private static CodeBlock assignationStatement(final FeatureParameter parameter,
												  final boolean raw,
												  final Group<?> ownerGroup)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		final var many = feature.many();

		if (many) return assignPatternMany(parameter);
		else if (raw) return assignPatternCast(parameter, ownerGroup);
		else return assignPatternSingle(parameter);
	}

	private static CodeBlock assignPatternMany(final FeatureParameter parameter)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		final var paramName = parameter.parameterName();
		return CodeBlock.of("this.$N.add($N)", feature.name(), paramName);
	}

	private static CodeBlock assignPatternCast(final FeatureParameter parameter, final Group<?> ownerGroup)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		final var paramName = parameter.parameterName();
		final var cast = ownerGroup != null
						 ? resolution.builderTypeFor(ownerGroup)
						 : parameter.parameterSpec().type;
		final var effectiveCast = cast instanceof ParameterizedTypeName param && param.rawType.equals(ConstantTypes.SUPPLIER)
								  ? param.rawType
								  : cast;
		return CodeBlock.of("this.$N = ($T) $N", feature.name(), effectiveCast, paramName);
	}

	private static CodeBlock assignPatternSingle(final FeatureParameter parameter)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		return CodeBlock.of("this.$N = $N", feature.name(), feature.name());
	}
}
