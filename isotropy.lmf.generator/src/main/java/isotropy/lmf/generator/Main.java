package isotropy.lmf.generator;

import isotropy.lmf.core.lang.Model;
import isotropy.lmf.core.resource.ResourceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

		try (final var modelInputStream = new FileInputStream(modelFile)		) {

			final var roots = ResourceUtil.loadModel(modelInputStream);

			System.out.println("roots = " + roots);

			roots.stream().map(Model.class::cast);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
