package isotropy.lmf.generator.group.impl;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.generator.code.util.CodeInstaller;
import isotropy.lmf.generator.code.feature.FeatureMethodBuilder;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.feature.FieldBuilder;
import isotropy.lmf.generator.code.feature.ParameterBuilder;
import isotropy.lmf.generator.code.type.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class ImplementationFeatureUtil
{
	private static final Modifier[] MODIFIERS = {Modifier.PUBLIC};
	private static final FeatureMethodBuilder GETTER_BUILDER = ImplementationFeatureUtil.getterBuilder();
	private static final FeatureMethodBuilder SETTER_BUILDER = ImplementationFeatureUtil.setterBuilder();
	private static final FieldBuilder FIELD_BUILDER = ImplementationFeatureUtil.fieldBuilder();
	private static final ParameterBuilder PARAMETER_BUILDER = ImplementationFeatureUtil.parameterBuilder();
	public static final LMGroupMethodBuilder LM_GROUP_METHOD_BUILDER = new LMGroupMethodBuilder();
	public static final SetMapMethodBuilder SETTERMAP_METHOD_BUILDER = new SetMapMethodBuilder();
	public static final GetMapMethodBuilder GETTERMAP_METHOD_BUILDER = new GetMapMethodBuilder();
	public static final GetMapFieldBuilder GETTERMAP_FIELD_BUILDER = new GetMapFieldBuilder();
	public static final SetMapFieldBuilder SETTERMAP_FIELD_BUILDER = new SetMapFieldBuilder();

	@SuppressWarnings("unchecked")
	public static CodeInstaller<FeatureResolution> buildFeatureInstallers(final TypeSpec.Builder classBuilder,
																		  final MethodSpec.Builder constructor)
	{
		return CodeInstaller.compose(CodeInstaller.of(GETTER_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(SETTER_BUILDER,
													  classBuilder::addMethod,
													  ImplementationFeatureUtil::setterPredicate),
									 CodeInstaller.of(FIELD_BUILDER, classBuilder::addField),
									 CodeInstaller.of(PARAMETER_BUILDER,
													  addConstructorParameter(constructor),
													  x -> true));
	}

	@SuppressWarnings("unchecked")
	public static CodeInstaller<TypeFeatures> buildTypeInstallers(final TypeSpec.Builder classBuilder)
	{
		return CodeInstaller.compose(CodeInstaller.of(LM_GROUP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(SETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(GETTERMAP_METHOD_BUILDER, classBuilder::addMethod),
									 CodeInstaller.of(GETTERMAP_FIELD_BUILDER, classBuilder::addField),
									 CodeInstaller.of(SETTERMAP_FIELD_BUILDER, classBuilder::addField));
	}

	private static boolean setterPredicate(final FeatureResolution f)
	{
		final var feature = f.feature();
		return !feature.many() && !feature.immutable();
	}

	private static FieldBuilder fieldBuilder()
	{
		return new FieldBuilder(FeatureResolution::name, ImplementationFeatureUtil::featureType);
	}

	private static ParameterBuilder parameterBuilder()
	{
		return new ParameterBuilder(FeatureResolution::name, ImplementationFeatureUtil::featureType);
	}

	private static FeatureMethodBuilder getterBuilder()
	{
		return new FeatureMethodBuilder(MODIFIERS,
										FeatureResolution::name,
										ImplementationFeatureUtil::featureType,
										Optional.empty(),
										Optional.of(ImplementationFeatureUtil::featureReturnStatement),
										true);
	}

	private static FeatureMethodBuilder setterBuilder()
	{
		return new FeatureMethodBuilder(MODIFIERS,
										FeatureResolution::name,
										f -> TypeName.VOID,
										Optional.of(ImplementationFeatureUtil::parameterType),
										Optional.of(ImplementationFeatureUtil::featureChangeStatement),
										true);
	}

	private static BiConsumer<ParameterSpec, FeatureResolution> addConstructorParameter(final MethodSpec.Builder constructor)
	{
		return (spec, resolution) -> {
			constructor.addParameter(spec);
			constructor.addStatement(assignationStatement(resolution));
			if (resolution.feature() instanceof Relation<?, ?>)
			{
				constructor.addStatement(containmentSetStatement(resolution));
			}
		};
	}

	private static List<String> featureChangeStatement(FeatureResolution resolution)
	{
		final var assignment = assignationStatement(resolution);
		final var notification = notificationStatement(resolution);
		final var containment = resolution.feature() instanceof Relation<?, ?> relation && relation.contains();

		return containment
			   ? List.of(assignment, containmentSetStatement(resolution), notification)
			   : List.of(assignment, notification);
	}

	private static String containmentSetStatement(final FeatureResolution resolution)
	{
		final var name = resolution.name();
		final var statement = "setContainer(%1$s, Features.%1$s)";
		return String.format(statement, name);
	}

	private static String notificationStatement(final FeatureResolution resolution)
	{
		final var name = resolution.name();
		final var statement = "lNotify(RelationNotificationBuilder.insert(this, Features.%1$s, %1$s))";
		return String.format(statement, name);
	}

	private static String assignationStatement(final FeatureResolution resolution)
	{
		final var assignPattern = resolution.feature()
											.many() ? "this.%1$s = List.copyOf(%1$s)" : "this.%1$s = %1$s";

		return String.format(assignPattern, resolution.name());
	}

	private static List<String> featureReturnStatement(FeatureResolution resolution)
	{
		final var name = resolution.name();
		return List.of(String.format("return %1$s", name));
	}

	private static ParameterSpec parameterType(FeatureResolution resolution)
	{
		final var singleType = resolution.singleType();

		return ParameterSpec.builder(singleType.parametrized(), resolution.name())
							.build();
	}

	private static TypeName featureType(FeatureResolution resolution)
	{
		return resolution.effectiveType()
						 .parametrized();
	}
}
