package org.logoce.lmf.generator.group.builder;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.feature.FeatureFieldBuilder;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.feature.FeatureParameter;
import org.logoce.lmf.generator.code.feature.MethodUtil;
import org.logoce.lmf.generator.code.type.*;
import org.logoce.lmf.generator.code.util.CodeInstaller;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.DefaultValueUtil;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.Relation;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class BuilderFeatureUtil
{
	private static final Modifier[] PUBLIC_ONLY = {Modifier.PUBLIC};
	public static final Modifier[] PRIVATE_ONLY = {Modifier.PRIVATE};
	private static final FeatureFieldBuilder FIELD_BUILDER = BuilderFeatureUtil.fieldBuilder();
	public static final AttributePushMethodBuilder ATTRIBUTE_PUSH_BUILDER = new AttributePushMethodBuilder();
	public static final RelationPushMethodBuilder RELATION_PUSH_BUILDER = new RelationPushMethodBuilder();

	@SuppressWarnings("unchecked")
	public static CodeInstaller<FeatureResolution> buildFeatureInstallers(final TypeSpec.Builder classBuilder,
																		  final TypeName builderType)
	{
		final var setterBuilder = BuilderFeatureUtil.setterBuilder(builderType);
		final var rawSetterBuilder = BuilderFeatureUtil.rawSetterBuilder(builderType);

		return CodeInstaller.compose(CodeInstaller.of(setterBuilder, classBuilder::addMethod),
									 CodeInstaller.of(FIELD_BUILDER, classBuilder::addField),
									 CodeInstaller.of(rawSetterBuilder,
													  classBuilder::addMethod,
													  FeatureResolution::hasGeneric));
	}

	@SuppressWarnings("unchecked")
	public static CodeInstaller<List<FeatureResolution>> buildTypeInstallers(final TypeSpec.Builder classBuilder,
																			 final Group<?> group)
	{
		final var attributeMapBuilder = new AttributeMapFieldBuilder(group);
		final var relationMapBuilder = new RelationMapFieldBuilder(group);
		final var buildMethodBuilder = new BuildMethodBuilder(group);

		return CodeInstaller.compose(CodeInstaller.of(buildMethodBuilder, classBuilder::addMethod),
									 CodeInstaller.of(ATTRIBUTE_PUSH_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(RELATION_PUSH_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(attributeMapBuilder, classBuilder::addField),
									 CodeInstaller.of(relationMapBuilder, classBuilder::addField));
	}

	private static FeatureFieldBuilder fieldBuilder()
	{
		return new FeatureFieldBuilder(true,
									   FeatureResolution::name,
									   FeatureResolution::builderType,
									   BuilderFeatureUtil::fieldInitializer);
	}

	private static Optional<CodeBlock> fieldInitializer(FeatureResolution resolution)
	{
		final var feature = resolution.feature();
		final var many = feature.many();
		final var immutable = feature.immutable();
		final var mandatory = feature.mandatory();

		if (many)
		{
			return Optional.of(CodeBlock.of("new $T<>((type, elements) -> {})", ConstantTypes.OBSERVABLE_LIST));
		}
		else if (immutable && !mandatory)
		{
			if (feature instanceof Relation<?, ?>)
			{
				return Optional.of(CodeBlock.of("() -> null"));
			}
			else
			{
				return DefaultValueUtil.resolveDefaultValue(resolution);
			}
		}
		else
		{
			return Optional.empty();
		}
	}

	private static FeatureMethodBuilder setterBuilder(TypeName returnType)
	{
		return new FeatureMethodBuilder(PUBLIC_ONLY,
										MethodUtil::builderMethodName,
										f -> returnType,
										Optional.of(FeatureResolution::builderParameterSpec),
										Optional.of(p -> featureChangeStatement(p, false)),
										List.of(ConstantTypes.OVERRIDE));
	}

	private static FeatureMethodBuilder rawSetterBuilder(TypeName returnType)
	{
		return new FeatureMethodBuilder(PRIVATE_ONLY,
										f -> '_' + f.name(),
										f -> returnType,
										Optional.of(f -> ParameterSpec.builder(f.feature() instanceof Relation<?, ?>
																			   ? ConstantTypes.SUPPLIER
																			   : f.singleType().raw(),
																			   MethodUtil.builderSingleParameterName(f),
																			   Modifier.FINAL).build()),
										Optional.of(p -> featureChangeStatement(p, true)),
										List.of(ConstantTypes.SUPPRESS_RAW_UNCHECKED));
	}

	private static List<CodeBlock> featureChangeStatement(final FeatureParameter parameter, final boolean raw)
	{
		return List.of(assignationStatement(parameter, raw), CodeBlock.of("return this"));
	}

	private static CodeBlock assignationStatement(final FeatureParameter parameter, final boolean raw)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		final var many = feature.many();
		final boolean isGenericAttribute = resolution.hasGeneric() && feature instanceof Attribute<?, ?>;
		final boolean needSupplyResolution = raw && isGenericAttribute;

		if (many) return assignPatternMany(parameter);
		else if (needSupplyResolution) return assignPatternCast(parameter);
		else return assignPatternSingle(parameter);
	}

	private static CodeBlock assignPatternMany(final FeatureParameter parameter)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		final var paramName = parameter.parameterName();
		return CodeBlock.of("this.$N.add($N)", feature.name(), paramName);
	}

	private static CodeBlock assignPatternCast(final FeatureParameter parameter)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		final var paramName = parameter.parameterName();
		final var cast = resolution.effectiveType().parametrized();
		return CodeBlock.of("this.$N = ($T) $N", feature.name(), cast, paramName);
	}

	private static CodeBlock assignPatternSingle(final FeatureParameter parameter)
	{
		final var resolution = parameter.feature();
		final var feature = resolution.feature();
		return CodeBlock.of("this.$N = $N", feature.name(), feature.name());
	}
}
