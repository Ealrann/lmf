package org.logoce.lmf.generator.group.impl;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.adapter.ModelResolution;
import org.logoce.lmf.generator.code.feature.FeatureFieldBuilder;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.feature.FeatureParameter;
import org.logoce.lmf.generator.code.type.*;
import org.logoce.lmf.generator.code.util.CodeInstaller;
import org.logoce.lmf.generator.code.util.ImplementationCodeUtil;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelUtils;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class ImplementationFeatureUtil
{
	private static final Modifier[] MODIFIERS = {Modifier.PUBLIC};
	private static final FeatureMethodBuilder GETTER_BUILDER = ImplementationFeatureUtil.getterBuilder();
	private static final FeatureMethodBuilder SETTER_BUILDER = ImplementationFeatureUtil.setterBuilder();
	private static final FeatureFieldBuilder FIELD_BUILDER = ImplementationFeatureUtil.fieldBuilder();
	private static final ConstructorBuilder CONSTRUCTOR_BUILDER = ImplementationFeatureUtil.parameterBuilder();
	public static final LMGroupMethodBuilder LM_GROUP_METHOD_BUILDER = new LMGroupMethodBuilder();
	public static final SetMapMethodBuilder SETTERMAP_METHOD_BUILDER = new SetMapMethodBuilder();
	public static final GetMapMethodBuilder GETTERMAP_METHOD_BUILDER = new GetMapMethodBuilder();

	@SuppressWarnings("unchecked")
	public static CodeInstaller<FeatureResolution> buildFeatureInstallers(final TypeSpec.Builder classBuilder)
	{
		return CodeInstaller.compose(CodeInstaller.of(GETTER_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(SETTER_BUILDER,
													  classBuilder::addMethod,
													  ImplementationFeatureUtil::setterPredicate),
									 CodeInstaller.of(FIELD_BUILDER, classBuilder::addField));
	}

	@SuppressWarnings("unchecked")
	public static CodeInstaller<Group<?>> buildTypeInstallers(final GroupInterfaceType interfaceGroupType,
															  final TypeSpec.Builder classBuilder)
	{
		final var getterMapFieldBuilder = new GetMapFieldBuilder(interfaceGroupType);
		final var setterMapFieldBuilder = new SetMapFieldBuilder(interfaceGroupType);

		return CodeInstaller.compose(CodeInstaller.of(LM_GROUP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(SETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(GETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(getterMapFieldBuilder, classBuilder::addField),
									 CodeInstaller.of(setterMapFieldBuilder, classBuilder::addField),
									 CodeInstaller.of(CONSTRUCTOR_BUILDER, classBuilder::addMethod));
	}

	private static boolean setterPredicate(final FeatureResolution f)
	{
		final var feature = f.feature();
		return !feature.many() && !feature.immutable();
	}

	private static FeatureFieldBuilder fieldBuilder()
	{
		return new FeatureFieldBuilder(false,
									   FeatureResolution::name,
									   ImplementationFeatureUtil::fieldFeatureType,
									   ImplementationFeatureUtil::fieldInitializer);
	}

	private static Optional<CodeBlock> fieldInitializer(FeatureResolution resolution)
	{
		final var feature = resolution.feature();
		final var many = feature.many();
		final var immutable = feature.immutable();

		if (many && !immutable)
		{
			return Optional.of(CodeBlock.of("new $T<>((type, elements) -> {})", ConstantTypes.OBSERVABLE_LIST));
		}
		else
		{
			return Optional.empty();
		}
	}

	private static ConstructorBuilder parameterBuilder()
	{
		return new ConstructorBuilder();
	}

	private static FeatureMethodBuilder getterBuilder()
	{
		return new FeatureMethodBuilder(MODIFIERS,
										FeatureResolution::name,
										ImplementationFeatureUtil::methodFeatureType,
										Optional.empty(),
										Optional.of(ImplementationCodeUtil::featureReturnStatement),
										List.of(ConstantTypes.OVERRIDE));
	}

	private static FeatureMethodBuilder setterBuilder()
	{
		return new FeatureMethodBuilder(MODIFIERS,
										FeatureResolution::name,
										f -> TypeName.VOID,
										Optional.of(FeatureResolution::parameterSpec),
										Optional.of(ImplementationFeatureUtil::featureChangeStatement),
										List.of());
	}

	private static List<CodeBlock> featureChangeStatement(FeatureParameter parameter)
	{
		final var paramName = parameter.parameterName();
		final var resolution = parameter.feature();
		final var feature = resolution.feature;
		final var assignment = ImplementationCodeUtil.assignationStatement(feature, paramName);
		final var containment = feature instanceof Relation<?, ?> relation && relation.contains();

		return containment
			   ? List.of(assignment, containmentSetStatement(resolution, paramName))
			   : List.of(assignment);
	}

	private static CodeBlock containmentSetStatement(final FeatureResolution resolution, final String paramName)
	{
		final var feature = resolution.feature();
		final var group = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtils.root(group);
		final var modelDefinition = model.adapt(ModelResolution.class).modelDefinition;
		final var constantGroupName = GenUtils.toConstantCase(group.name());
		final var constantFeatureName = GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("setContainer($N, $T.Features.$N.$N)",
							paramName,
							modelDefinition,
							constantGroupName,
							constantFeatureName);
	}

	private static TypeName fieldFeatureType(FeatureResolution resolution)
	{
		return resolution.implementationType().parametrized();
	}

	private static TypeName methodFeatureType(FeatureResolution resolution)
	{
		return resolution.effectiveType().parametrized();
	}
}
