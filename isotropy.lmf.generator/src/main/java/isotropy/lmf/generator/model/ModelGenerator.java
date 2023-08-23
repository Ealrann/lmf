package isotropy.lmf.generator.model;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Model;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class ModelGenerator
{
	private final Model model;

	public ModelGenerator(Model model)
	{
		this.model = model;
	}

	public void generateJava(File target)
	{

		for(final var group : model.groups())
		{
			final var groupGenerator = new GroupGenerator(group);
			groupGenerator.generate(target);
		}


		final var modelName = model.name();
		final var className = modelName + "Package";
		final var packageName = model.domain();

		TypeSpec iface = TypeSpec.interfaceBuilder("HelloWorld")
								 //.addModifiers(Modifier.PUBLIC)
								 .addField(FieldSpec.builder(String.class, "ONLY_THING_THAT_IS_CONSTANT")
													.addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
													.initializer("$S", "change")
													.build())
								 .addMethod(MethodSpec.methodBuilder("beep")
													  .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
													  .build())
								 .build();

		MethodSpec main = MethodSpec.methodBuilder("main")
									.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
									.returns(void.class)
									.addParameter(String[].class, "args")
									.addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
									.build();

		TypeSpec helloWorld = TypeSpec.classBuilder(className)
									  .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
									  .addMethod(main)
									  .addType(iface)
									  .build();

		JavaFile javaFile = JavaFile.builder(packageName, helloWorld)
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
