package org.logoce.lmf.generator.model;

import com.squareup.javapoet.*;
import org.logoce.lmf.generator.util.BuilderInitializerUtil;
import org.logoce.lmf.generator.util.ConstantTypes;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.generator.util.TypeParameter;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.MetaModel;

import javax.lang.model.element.Modifier;
import java.io.File;

public class ModelPackage
{
	public static final TypeVariableName T = TypeVariableName.get("T");
	private static final TypeVariableName T_EXTENDS_LMOBJECT = TypeVariableName.get("T", ConstantTypes.LM_OBJECT);
	private static final ClassName ROOT_BUILDER = ClassName.get(IFeaturedObject.Builder.class);
	private static final TypeName ROOT_BUILDER_OF_T = TypeParameter.of(ROOT_BUILDER, T_EXTENDS_LMOBJECT)
																   .nestIn(ConstantTypes.OPTIONAL)
																   .parametrized();

	private final MetaModel model;

	public ModelPackage(MetaModel model)
	{
		this.model = model;
	}

	public void generate(final File target)
	{
		final var currentClass = ClassName.get(TargetPathUtil.packageName(model),
											   model.name() + "ModelPackage");
		final var definitionName = model.name() + "ModelDefinition";

		final var packageClass = TypeSpec.classBuilder(currentClass)
										 .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
										 .addSuperinterface(ConstantTypes.IMODEL_PACKAGE);

		packageClass.addField(FieldSpec.builder(currentClass,
												"Instance",
												Modifier.PUBLIC,
												Modifier.STATIC,
												Modifier.FINAL).initializer("new $T()", currentClass).build());

		final var modelInitializer = buildModelInitializer(definitionName);

		packageClass.addField(FieldSpec.builder(ConstantTypes.MODEL,
												"MODEL",
												Modifier.PUBLIC,
												Modifier.STATIC,
												Modifier.FINAL)
									   .initializer("$L", modelInitializer)
									   .build());

		packageClass.addMethod(MethodSpec.methodBuilder("model")
										 .addModifiers(Modifier.PUBLIC)
										 .returns(ConstantTypes.MODEL)
										 .addStatement("return MODEL")
										 .addAnnotation(ConstantTypes.OVERRIDE)
										 .build());

		packageClass.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

		packageClass.addMethod(buildBuilderResolver(definitionName));
		packageClass.addMethod(buildEnumResolver(definitionName));

		final var javaFile = JavaFile.builder(TargetPathUtil.packageName(model), packageClass.build())
									 .skipJavaLangImports(true)
									 .build();
		FormattedJavaWriter.write(javaFile, target);
	}

	private MethodSpec buildBuilderResolver(final String definitionName)
	{
		final var methodBuilder = MethodSpec.methodBuilder("builder")
											.addModifiers(Modifier.PUBLIC)
											.addAnnotation(Override.class)
											.addAnnotation(ConstantTypes.SUPPRESS_UNCHECKED)
											.returns(ROOT_BUILDER_OF_T)
											.addTypeVariable(T_EXTENDS_LMOBJECT)
											.addParameter(ConstantTypes.GROUP.nest(T_EXTENDS_LMOBJECT).parametrized(),
														  "group");

		installBuilderResolutionStatements(definitionName, methodBuilder);
		return methodBuilder.build();
	}

	private void installBuilderResolutionStatements(final String definitionName, final MethodSpec.Builder methodBuilder)
	{
		boolean first = true;
		for (final var group : model.groups())
		{
			if (group.concrete())
			{
				final var statement = CodeBlock.builder();
				if (first) first = false;
				else statement.add("else ");

				final var groupName = group.name();
				final var groupConstantName = GenUtils.toConstantCase(groupName);
				statement.add("if (group == $N.Groups.$N) return Optional.of((IFeaturedObject.Builder<T>) $N.builder" +
							  "())", definitionName, groupConstantName, groupName);
				final var build = statement.build();
				methodBuilder.addStatement(build);
			}
		}
		methodBuilder.addStatement("return $T.empty()", ConstantTypes.OPTIONAL);
	}

	private MethodSpec buildEnumResolver(final String definitionName)
	{
		final var enumResolve = MethodSpec.methodBuilder("resolveEnumLiteral")
										  .addModifiers(Modifier.PUBLIC)
										  .addAnnotation(Override.class)
										  .addAnnotation(ConstantTypes.SUPPRESS_UNCHECKED)
										  .returns(TypeParameter.of(ConstantTypes.OPTIONAL, T).parametrized())
										  .addTypeVariable(T)
										  .addParameter(ConstantTypes.ENUM.nest(T).parametrized(), "_enum")
										  .addParameter(ConstantTypes.STRING, "value");

		installEnumResolutionStatements(definitionName, enumResolve);
		return enumResolve.build();
	}

	private void installEnumResolutionStatements(final String definitionName, final MethodSpec.Builder enumResolve)
	{
		boolean first = true;
		for (final var _enum : model.enums())
		{
			final var statement = CodeBlock.builder();
			if (first) first = false;
			else statement.add("else ");

			final var enumName = _enum.name();
			final var enumConstantName = GenUtils.toConstantCase(enumName);
			statement.add("if (_enum == $N.Enums.$N) return (Optional<T>) Optional.of($N" + ".valueOf(value))",
						  definitionName,
						  enumConstantName,
						  enumName);
			final var build = statement.build();
			enumResolve.addStatement(build);
		}
		enumResolve.addStatement("return $T.empty()", ConstantTypes.OPTIONAL);
	}

	private CodeBlock buildModelInitializer(final String definitionName)
	{
		final var initializer = CodeBlock.builder()
										 .add("new $T()", ConstantTypes.META_MODEL_BUILDER);

		BuilderInitializerUtil.appendAttributes(model,
												initializer,
												attribute -> !"lmPackage".equals(attribute.name()));

		initializer.add(".lmPackage(Instance)");

		initializer.add(".addGroups($N.Groups.ALL)", definitionName);
		initializer.add(".addEnums($N.Enums.ALL)", definitionName);
		initializer.add(".addUnits($N.Units.ALL)", definitionName);
		initializer.add(".addAliases($N.Aliases.ALL)", definitionName);
		initializer.add(".addJavaWrappers($N.JavaWrappers.ALL)", definitionName);

		initializer.add(".build()");
		return initializer.build();
	}
}
