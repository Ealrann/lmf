package isotropy.lmf.generator.model;

import com.squareup.javapoet.*;
import isotropy.lmf.core.lang.Group;
import isotropy.lmf.core.lang.LMCorePackage;
import isotropy.lmf.core.lang.LMObject;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.model.IFeaturedObject;
import isotropy.lmf.core.util.ModelUtils;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class GroupGenerator
{
	private final Group<?> group;

	public GroupGenerator(Group<?> group)
	{
		this.group = group;
	}

	public void generate(final File target)
	{
		generateInterface(target);
	}

	private void generateInterface(final File target)
	{
		final var groupName = group.name();
		final var packageName = "isotropy.lmf.lang";
		final var isFinal = group.concrete();

		final var includes = group.includes();
		final var refInclude = includes.isEmpty() ? null : includes.get(0);

		final var groupInterfaceBuilder = TypeSpec.interfaceBuilder(groupName)
												  .addModifiers(Modifier.PUBLIC)
												  .addSuperinterface(ParameterizedTypeName.get(ClassName.get(
														  IFeaturedObject.class), ClassName.get("", groupName)));
		final var builderTypeBuilder = TypeSpec.interfaceBuilder("Builder")
											   .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
											   .addSuperinterface(ParameterizedTypeName.get(ClassName.get(
													   IFeaturedObject.Builder.class), ClassName.get("", groupName)));

		if (refInclude != null)
		{
			final var model = (Model) refInclude.lContainer();

			groupInterfaceBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(model.domain(), ""),
																			  ClassName.get("", "T")));
			builderTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IFeaturedObject.Builder.class),
																		   ClassName.get("", "T")));
		}
		else if (groupName.equals("LMObject"))
		{
			groupInterfaceBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IFeaturedObject.class),
																			  ClassName.get("", "T")));
			builderTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IFeaturedObject.Builder.class),
																		   ClassName.get("", "T")));
		}
		else
		{
			groupInterfaceBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(LMObject.class),
																			  ClassName.get("", "T")));
			builderTypeBuilder.addSuperinterface(ParameterizedTypeName.get(ClassName.get(LMObject.Builder.class),
																		   ClassName.get("", "T")));
		}

		for (final var feature : group.features())
		{
			final var builderFeatureMethod = MethodSpec.methodBuilder(feature.name())
													   .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
													   .returns(void.class)
													   .build();

			final var featureMethod = MethodSpec.methodBuilder(feature.name())
												.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
												.returns(void.class)
												.build();

			builderTypeBuilder.addMethod(builderFeatureMethod);
			groupInterfaceBuilder.addMethod(featureMethod);
		}

		final var builderType = builderTypeBuilder.build();
		final var groupClass = groupInterfaceBuilder.addType(builderType)
													.build();
		final var javaFile = JavaFile.builder(packageName, groupClass)
									 .build();

		try
		{
			javaFile.writeTo(target);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
