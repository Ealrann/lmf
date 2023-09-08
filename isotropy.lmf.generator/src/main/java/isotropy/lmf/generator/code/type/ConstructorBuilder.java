package isotropy.lmf.generator.code.type;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.generator.code.feature.FeatureResolution;
import isotropy.lmf.generator.code.util.CodeBuilder;
import isotropy.lmf.generator.code.util.ImplementationCodeUtil;
import isotropy.lmf.generator.group.GroupGenerationContext;

import javax.lang.model.element.Modifier;
import java.util.Optional;

public final class ConstructorBuilder implements CodeBuilder<GroupGenerationContext, MethodSpec>
{
	public ConstructorBuilder()
	{
	}

	@Override
	public MethodSpec build(GroupGenerationContext context)
	{
		final var constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		final var codeList = context.featureResolutions().stream().map(ConstructorBuilder::bakeCode).toList();

		codeList.forEach(c -> c.installStep1(constructor));

		codeList.stream()
				.map(ParameterCode::notif)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(constructor::addStatement);

		return constructor.build();
	}

	private static ParameterCode bakeCode(FeatureResolution resolution)
	{
		final var paramSpec = resolution.parameterSpec();
		final var assignCode = ImplementationCodeUtil.assignationStatement(resolution.feature(), paramSpec.name);
		final var isContainment = resolution.feature() instanceof Relation<?, ?> r && r.contains();

		return new ParameterCode(paramSpec,
								 assignCode,
								 isContainment
								 ? Optional.of(ImplementationCodeUtil.containmentSetStatement(paramSpec.name))
								 : Optional.empty());
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
