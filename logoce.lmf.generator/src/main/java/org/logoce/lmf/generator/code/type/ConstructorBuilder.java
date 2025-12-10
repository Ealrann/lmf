package org.logoce.lmf.generator.code.type;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import org.logoce.lmf.generator.adapter.FeatureResolution;
import org.logoce.lmf.generator.code.util.CodeBuilder;
import org.logoce.lmf.generator.code.util.ImplementationCodeUtil;
import org.logoce.lmf.generator.util.FeatureStreams;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;

import javax.lang.model.element.Modifier;
import java.util.Optional;

public final class ConstructorBuilder implements CodeBuilder<Group<?>, MethodSpec>
{
	public ConstructorBuilder()
	{
	}

	@Override
	public MethodSpec build(Group<?> group)
	{
		final var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		final var codeList = FeatureStreams.distinctFeatures(group)
										   .map(g -> g.adapt(FeatureResolution.class))
										   .filter(ConstructorBuilder::mandatoryOrImmutable)
										   .map(ConstructorBuilder::bakeCode)
										   .toList();

		codeList.forEach(c -> c.installStep1(constructor));

		codeList.stream()
				.map(ParameterCode::notif)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(constructor::addStatement);

		constructor.addStatement("eDeliver(true)");

		return constructor.build();
	}

	private static boolean mandatoryOrImmutable(final FeatureResolution resolution)
	{
		final var feature = resolution.feature();
		return feature.immutable() || feature.mandatory();
	}

	private static ParameterCode bakeCode(final FeatureResolution resolution)
	{
		final var paramSpec = resolution.parameterSpec();
		final var feature = resolution.feature();
		final var assignCode = ImplementationCodeUtil.assignationStatement(feature, paramSpec.name);

		final var isRelation = feature instanceof Relation<?, ?>;
		final var isContainment = isRelation && ((Relation<?, ?>) feature).contains();
		final var manyMutableRelation = isRelation && feature.many() && !feature.immutable();

		return new ParameterCode(paramSpec,
								 assignCode,
								 isContainment && !manyMutableRelation
								 ? Optional.of(containmentSetStatement(resolution, paramSpec.name))
								 : Optional.empty());
	}

	private static CodeBlock containmentSetStatement(final FeatureResolution resolution, final String paramName)
	{
		final var feature = resolution.feature();
		final var group = (Group<?>) feature.lmContainer();
		final var model = (MetaModel) ModelUtil.root(group);
		final var groupType = ClassName.get(TargetPathUtil.packageName(model), group.name());
		final var constantName = org.logoce.lmf.generator.util.GenUtils.toConstantCase(feature.name());

		return CodeBlock.of("setContainer($N, $T.FeatureIDs.$N)",
							paramName,
							groupType,
							constantName);
	}

	private record ParameterCode(ParameterSpec parameterSpec, CodeBlock assign, Optional<CodeBlock> notif)
	{
		public void installStep1(MethodSpec.Builder constructor)
		{
			constructor.addParameter(parameterSpec);
			constructor.addStatement(assign);
		}
	}
}
