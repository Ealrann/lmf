package isotropy.lmf.generator.group.impl;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.generator.group.feature.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class ImplementationFeatureUtil
{
	private static final Modifier[] MODIFIERS = {Modifier.PUBLIC};
	private static final MethodBuilder GETTER_BUILDER = ImplementationFeatureUtil.getterBuilder();
	private static final MethodBuilder SETTER_BUILDER = ImplementationFeatureUtil.setterBuilder();
	private static final FieldBuilder FIELD_BUILDER = ImplementationFeatureUtil.fieldBuilder();
	private static final ParameterBuilder PARAMETER_BUILDER = ImplementationFeatureUtil.parameterBuilder();

	public static FeatureInstallers buildInstallers(final TypeSpec.Builder classBuilder,
													final MethodSpec.Builder constructor)
	{
		final var getterInstaller = new FeatureInstaller<>(GETTER_BUILDER, classBuilder::addMethod);
		final var setterInstaller = new FeatureInstaller<>(SETTER_BUILDER,
														   classBuilder::addMethod,
														   ImplementationFeatureUtil::setterPredicate);
		final var fieldInstaller = new FeatureInstaller<>(FIELD_BUILDER, classBuilder::addField);
		final var parameterInstaller = new FeatureInstaller<>(PARAMETER_BUILDER,
															  addConstructorParameter(constructor),
															  x -> true);

		return new FeatureInstallers(List.of(getterInstaller, setterInstaller, fieldInstaller, parameterInstaller));
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

	private static MethodBuilder getterBuilder()
	{
		return new MethodBuilder(MODIFIERS,
								 FeatureResolution::name,
								 ImplementationFeatureUtil::featureType,
								 Optional.empty(),
								 Optional.of(ImplementationFeatureUtil::featureReturnStatement));
	}

	private static MethodBuilder setterBuilder()
	{
		return new MethodBuilder(MODIFIERS,
								 FeatureResolution::name,
								 f -> TypeName.VOID,
								 Optional.of(ImplementationFeatureUtil::parameterType),
								 Optional.of(ImplementationFeatureUtil::featureAssignStatement));
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

	private static List<String> featureAssignStatement(FeatureResolution resolution)
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
