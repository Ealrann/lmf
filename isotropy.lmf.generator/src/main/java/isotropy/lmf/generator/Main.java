package isotropy.lmf.generator;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.resource.ResourceUtil;
import isotropy.lmf.generator.model.ModelGenerator;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main
{
	public static void main(String[] args)
	{
		final var modelPath = args[0];
		final var targetPath = args[1];

		System.out.println("modelPath = " + modelPath);
		System.out.println("targetDir = " + targetPath);

		final var modelFile = new File(modelPath);
		final var targetDir = new File(targetPath);

		if (targetDir.exists() == false)
		{
			targetDir.mkdir();
		}

		try (final var modelInputStream = new FileInputStream(modelFile))
		{
			final var roots = ResourceUtil.loadModel(modelInputStream);

			for(final var root : roots)
			{
				if(root instanceof Model model)
				{
					final var generator = new ModelGenerator(model);
					System.out.println("Generating = " + model.name() + "...");
					generator.generateJava(targetDir);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
