package isotropy.lmf.generator.model;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.util.ModelUtils;
import isotropy.lmf.generator.util.GenUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class GroupGenerator
{
	private final Group<?> group;
	private final String packageName;

	public GroupGenerator(Group<?> group)
	{
		this.group = group;
		final var model = (Model) group.lmContainer();
		packageName = model.domain();
	}

	public void generate(final File target)
	{
		final var isFinal = group.concrete();
		final var includes = group.includes();
		final var refInclude = includes.isEmpty() ? null : includes.get(0);
		final var types = Types.build(refInclude, group);

		final var interfaceBuilder = TypeSpec.interfaceBuilder(types.className())
											 .addSuperinterface(types.superType())
											 .addTypeVariables(types.typedParameters())
											 .addModifiers(Modifier.PUBLIC);

		if (isFinal)
		{
			final var builderTypes = types.builder();
			final var builderTypeBuilder = TypeSpec.interfaceBuilder(builderTypes.className())
												   .addSuperinterface(builderTypes.superType())
												   .addTypeVariables(builderTypes.typedParameters())
												   .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

			final var typedBuilder = GenUtils.parameterize(builderTypes.className(), builderTypes.rawParameters());
			final var methodBuilder = new MethodUtil.BuilderMethodBuilder(typedBuilder);

			ModelUtils.streamAllFeatures(group)
					  .map(methodBuilder::build)
					  .forEach(builderTypeBuilder::addMethod);

			interfaceBuilder.addType(builderTypeBuilder.build());
		}

		final var methodBuilder = new MethodUtil.MethodBuilder();

		group.features()
			 .stream()
			 .map(methodBuilder::build)
			 .forEach(interfaceBuilder::addMethod);

		try
		{
			final var javaFile = JavaFile.builder(packageName, interfaceBuilder.build())
										 .build();
			javaFile.writeTo(target);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
