package org.logoce.lmf.generator.group.impl;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.adapter.GroupInterfaceType;
import org.logoce.lmf.generator.code.feature.FeatureFieldBuilder;
import org.logoce.lmf.generator.code.feature.FeatureMethodBuilder;
import org.logoce.lmf.generator.code.feature.FeatureParameter;
import org.logoce.lmf.generator.code.type.*;
import org.logoce.lmf.generator.code.util.CodeInstaller;
import org.logoce.lmf.generator.code.util.ImplementationCodeUtil;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelUtil;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class ImplementationFeatureUtil
{
	private static final Modifier[] MODIFIERS = {Modifier.PUBLIC};
	private static final ClassName SET_NOTIFICATION_TYPE = ClassName.get(
			"org.logoce.lmf.model.notification.impl", "SetNotification");
	private static final ConstructorBuilder CONSTRUCTOR_BUILDER = ImplementationFeatureUtil.parameterBuilder();
	public static final LMGroupMethodBuilder LM_GROUP_METHOD_BUILDER = new LMGroupMethodBuilder();
	public static final SetMapMethodBuilder SETTERMAP_METHOD_BUILDER = new SetMapMethodBuilder();
	public static final GetMapMethodBuilder GETTERMAP_METHOD_BUILDER = new GetMapMethodBuilder();

	public static CodeInstaller<FeatureResolution> buildFeatureInstallers(final TypeSpec.Builder classBuilder,
																		  final Group<?> ownerGroup)
	{
		final var getterBuilder = getterBuilder(ownerGroup);
		final var setterBuilder = setterBuilder(ownerGroup);
		final var fieldBuilder = fieldBuilder(ownerGroup);

		return CodeInstaller.compose(CodeInstaller.of(getterBuilder, classBuilder::addMethod),
									 CodeInstaller.of(setterBuilder,
													  classBuilder::addMethod,
													  ImplementationFeatureUtil::setterPredicate),
									 CodeInstaller.of(fieldBuilder, classBuilder::addField));
	}

	public static CodeInstaller<Group<?>> buildTypeInstallers(final GroupInterfaceType interfaceGroupType,
															  final TypeSpec.Builder classBuilder)
	{
		final var mapHolderBuilder = new GetterSetterMapHolderBuilder(interfaceGroupType);

		return CodeInstaller.compose(CodeInstaller.of(LM_GROUP_METHOD_BUILDER, classBuilder::addMethod),
				CodeInstaller.of(SETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
				CodeInstaller.of(GETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
				CodeInstaller.of(mapHolderBuilder, classBuilder::addType),
				CodeInstaller.of(CONSTRUCTOR_BUILDER, classBuilder::addMethod));
	}

	private static boolean setterPredicate(final FeatureResolution f)
	{
		final var feature = f.feature();
		return !feature.many() && !feature.immutable();
	}

	private static FeatureFieldBuilder fieldBuilder(final Group<?> ownerGroup)
	{
		return new FeatureFieldBuilder(false,
									   FeatureResolution::name,
									   f -> fieldFeatureType(f, ownerGroup),
									   ImplementationFeatureUtil::fieldInitializer);
	}

	private static Optional<CodeBlock> fieldInitializer(FeatureResolution resolution)
	{
		final var feature = resolution.feature();
		final var many = feature.many();
		final var immutable = feature.immutable();

		if (!many || immutable) return Optional.empty();

		final var group = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(group);
		final var groupType = ClassName.get(TargetPathUtil.packageName(model), group.name());
		final var constantName = GenUtils.toConstantCase(feature.name());
		final var isRelation = feature instanceof Relation<?, ?, ?, ?>;
		final var isContainment = feature instanceof Relation<?, ?, ?, ?> relation && relation.contains();

		return Optional.of(CodeBlock.of("newObservableList($T.FeatureIDs.$N, $L, $L)",
										groupType,
										constantName,
										isRelation,
										isContainment));
	}

	private static ConstructorBuilder parameterBuilder()
	{
		return new ConstructorBuilder();
	}

	private static FeatureMethodBuilder getterBuilder(final Group<?> ownerGroup)
	{
		return new FeatureMethodBuilder(MODIFIERS,
										FeatureResolution::name,
										f -> methodFeatureType(f, ownerGroup),
										Optional.empty(),
										Optional.of(ImplementationCodeUtil::featureReturnStatement),
										List.of(ConstantTypes.OVERRIDE));
	}

	private static FeatureMethodBuilder setterBuilder(final Group<?> ownerGroup)
	{
		return new FeatureMethodBuilder(MODIFIERS,
										FeatureResolution::name,
										f -> TypeName.VOID,
										Optional.of(f -> f.parameterSpec(ownerGroup)),
										Optional.of(ImplementationFeatureUtil::featureChangeStatement),
										List.of(ConstantTypes.OVERRIDE));
	}

	private static List<CodeBlock> featureChangeStatement(FeatureParameter parameter)
	{
		final var paramName = parameter.parameterName();
		final var resolution = parameter.feature();
		final var feature = resolution.feature;
		final var assignment = ImplementationCodeUtil.assignationStatement(feature, paramName);

		final var group = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(group);
		final var groupType = ClassName.get(TargetPathUtil.packageName(model), group.name());
		final var constantName = GenUtils.toConstantCase(feature.name());
		final var featureIdExpr = CodeBlock.of("$T.FeatureIDs.$N", groupType, constantName);

		final var oldValue = CodeBlock.of("final var oldValue = this.$N", feature.name());
		final var containment = feature instanceof Relation<?, ?, ?, ?> relation && relation.contains();
		final var containmentFlag = containment ? CodeBlock.of("true") : CodeBlock.of("false");
		if (containment)
		{
			final var setContainer = containmentSetStatement(featureIdExpr, paramName);
			return List.of(oldValue,
						   assignment,
						   setContainer,
						   CodeBlock.of("eNotify(new $T(this, $L, $L, $N, oldValue))",
										SET_NOTIFICATION_TYPE,
										containmentFlag,
										featureIdExpr,
										paramName));
		}
		else
		{
			return List.of(oldValue,
						   assignment,
						   CodeBlock.of("eNotify(new $T(this, $L, $L, $N, oldValue))",
										SET_NOTIFICATION_TYPE,
										containmentFlag,
										featureIdExpr,
										paramName));
		}
	}

	private static CodeBlock containmentSetStatement(final CodeBlock featureExpr, final String paramName)
	{
		return CodeBlock.of("setContainer($N, $L)", paramName, featureExpr);
	}

	private static TypeName fieldFeatureType(FeatureResolution resolution, Group<?> owner)
	{
		return resolution.implementationTypeFor(owner).parametrized();
	}

	private static TypeName methodFeatureType(FeatureResolution resolution, Group<?> owner)
	{
		return resolution.effectiveTypeFor(owner).parametrized();
	}
}
