package isotropy.lmf.generator.group.builder;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.generator.code.feature.*;
import isotropy.lmf.generator.code.type.*;
import isotropy.lmf.generator.code.util.CodeInstaller;
import isotropy.lmf.generator.util.GroupType;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public final class BuilderFeatureUtil
{
	private static final Modifier[] MODIFIERS = {Modifier.PUBLIC};
	private static final FeatureFieldBuilder FIELD_BUILDER = BuilderFeatureUtil.fieldBuilder();
	public static final AttributePushMethodBuilder ATTRIBUTE_PUSH_BUILDER = new AttributePushMethodBuilder();
	public static final RelationPushMethodBuilder RELATION_PUSH_BUILDER = new RelationPushMethodBuilder();

	@SuppressWarnings("unchecked")
	public static CodeInstaller<FeatureResolution> buildFeatureInstallers(final TypeSpec.Builder classBuilder,
																		  final TypeName builderType)
	{
		final var setterBuilder = BuilderFeatureUtil.setterBuilder(builderType);

		return CodeInstaller.compose(CodeInstaller.of(setterBuilder, classBuilder::addMethod),
									 CodeInstaller.of(FIELD_BUILDER, classBuilder::addField));
	}

	@SuppressWarnings("unchecked")
	public static CodeInstaller<List<FeatureResolution>> buildTypeInstallers(final TypeSpec.Builder classBuilder,
																			 final GroupType groupType)
	{
		final var builderType = groupType.builderClass();
		final var attributeMapBuilder = new AttributeMapFieldBuilder(groupType, builderType);
		final var relationMapBuilder = new RelationMapFieldBuilder(groupType, builderType);
		final var buildMethodBuilder = new BuildMethodBuilder(groupType, builderType);

		return CodeInstaller.compose(CodeInstaller.of(buildMethodBuilder, classBuilder::addMethod),
									 CodeInstaller.of(ATTRIBUTE_PUSH_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(RELATION_PUSH_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(attributeMapBuilder, classBuilder::addField),
									 CodeInstaller.of(relationMapBuilder, classBuilder::addField));
	}

	private static FeatureFieldBuilder fieldBuilder()
	{
		return new FeatureFieldBuilder(true, FeatureResolution::name, FeatureResolution::builderType);
	}

	private static FeatureMethodBuilder setterBuilder(TypeName returnType)
	{
		return new FeatureMethodBuilder(MODIFIERS,
										MethodUtil::builderMethodName,
										f -> returnType,
										Optional.of(FeatureResolution::builderParameterSpec),
										Optional.of(BuilderFeatureUtil::featureChangeStatement),
										true);
	}

	private static List<CodeBlock> featureChangeStatement(final FeatureParameter parameter)
	{
		return List.of(assignationStatement(parameter), CodeBlock.of("return this"));
	}

	private static CodeBlock assignationStatement(final FeatureParameter parameter)
	{
		final var feature = parameter.feature().feature();
		final var many = feature.many();
		final var paramName = parameter.parameterName();
		final var assignPattern = many ? "this." + feature.name() + ".add(%1$s)" : "this.%1$s = %1$s";

		return CodeBlock.of(String.format(assignPattern, paramName));
	}
}
