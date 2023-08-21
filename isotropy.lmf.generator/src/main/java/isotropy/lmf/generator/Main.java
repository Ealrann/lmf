package isotropy.lmf.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.resource.ResourceUtil;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main
{
	public static void main(String[] args)
	{
		final var modelPath = args[0];
		final var targetDir = args[1];

		System.out.println("modelPath = " + modelPath);
		System.out.println("targetDir = " + targetDir);

		final var modelFile = new File(modelPath);
		final var targetPath = new File(targetDir);

		try (final var modelInputStream = new FileInputStream(modelFile))
		{

			final var roots = ResourceUtil.loadModel(modelInputStream);

			System.out.println("roots = " + roots);

			roots.stream()
				 .map(Model.class::cast);

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

			TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
										  .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
										  .addMethod(main)
										  .addType(iface)
										  .build();

			JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
										.build();

			javaFile.writeTo(System.out);

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
