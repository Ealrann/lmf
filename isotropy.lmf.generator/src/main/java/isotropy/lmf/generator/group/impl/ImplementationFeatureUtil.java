package isotropy.lmf.generator.group.impl;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.generator.code.feature.FeatureFieldBuilder;
import isotropy.lmf.generator.code.feature.FeatureMethodBuilder;
import isotropy.lmf.generator.code.feature.FeatureParameter;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.type.*;
import isotropy.lmf.generator.code.util.CodeInstaller;
import isotropy.lmf.generator.code.util.ImplementationCodeUtil;
import isotropy.lmf.generator.group.GroupGenerationContext;
import isotropy.lmf.generator.util.ConstantTypes;

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
	public static final GetMapFieldBuilder GETTERMAP_FIELD_BUILDER = new GetMapFieldBuilder();
	public static final SetMapFieldBuilder SETTERMAP_FIELD_BUILDER = new SetMapFieldBuilder();

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
	public static CodeInstaller<GroupGenerationContext> buildTypeInstallers(final TypeSpec.Builder classBuilder)
	{
		return CodeInstaller.compose(CodeInstaller.of(LM_GROUP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(SETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(GETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(GETTERMAP_FIELD_BUILDER, classBuilder::addField),
									 CodeInstaller.of(SETTERMAP_FIELD_BUILDER, classBuilder::addField),
									 CodeInstaller.of(CONSTRUCTOR_BUILDER, classBuilder::addMethod));
	}

	private static boolean setterPredicate(final FeatureResolution f)
	{
		final var feature = f.feature();
		return !feature.many() && !feature.immutable();
	}

	private static FeatureFieldBuilder fieldBuilder()
	{
		return new FeatureFieldBuilder(false, FeatureResolution::name, ImplementationFeatureUtil::fieldFeatureType);
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
										List.of(ConstantTypes.OVERRIDE));
	}

	private static List<CodeBlock> featureChangeStatement(FeatureParameter parameter)
	{
		final var paramName = parameter.parameterName();
		final var feature = parameter.feature().feature();
		final var assignment = ImplementationCodeUtil.assignationStatement(feature, paramName);
		final var notification = ImplementationCodeUtil.notificationStatement(paramName);
		final var containment = feature instanceof Relation<?, ?> relation && relation.contains();

		return containment ? List.of(assignment,
									 ImplementationCodeUtil.containmentSetStatement(paramName),
									 notification) : List.of(assignment, notification);
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
