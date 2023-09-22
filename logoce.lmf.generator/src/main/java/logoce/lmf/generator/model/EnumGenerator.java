package logoce.lmf.generator.model;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import logoce.lmf.generator.util.GenUtils;
import logoce.lmf.model.lang.Enum;
import logoce.lmf.model.lang.Model;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;

public class EnumGenerator
{
	private final Enum<?> enumeration;
	private final String packageName;

	public EnumGenerator(Enum<?> enumeration)
	{
		this.enumeration = enumeration;
		final var model = (Model) enumeration.lmContainer();
		packageName = model.domain();
	}

	public void generate(final File target)
	{

		final var enumBuilder = TypeSpec.enumBuilder(enumeration.name())
										.addModifiers(Modifier.PUBLIC);

		for (final var litteral : enumeration.literals())
		{
			enumBuilder.addEnumConstant(GenUtils.capitalizeFirstLetter(litteral));
		}

		final var javaFile = JavaFile.builder(packageName, enumBuilder.build())
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
