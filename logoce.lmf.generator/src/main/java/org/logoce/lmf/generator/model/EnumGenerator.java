package org.logoce.lmf.generator.model;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.logoce.lmf.generator.util.FormattedJavaWriter;
import org.logoce.lmf.generator.util.GenUtils;
import org.logoce.lmf.generator.util.TargetPathUtil;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.MetaModel;

import javax.lang.model.element.Modifier;
import java.io.File;

public class EnumGenerator
{
	private final Enum<?> enumeration;
	private final String packageName;

	public EnumGenerator(Enum<?> enumeration)
	{
		this.enumeration = enumeration;
		final var model = (MetaModel) enumeration.lmContainer();
		packageName = TargetPathUtil.packageName(model);
	}

	public void generate(final File target)
	{

		final var enumBuilder = TypeSpec.enumBuilder(enumeration.name())
										.addModifiers(Modifier.PUBLIC);

		for (final var litteral : enumeration.literals())
		{
			enumBuilder.addEnumConstant(GenUtils.capitalizeFirstLetter(litteral));
		}

		final var javaFile = JavaFile.builder(packageName, enumBuilder.build()).build();
		FormattedJavaWriter.write(javaFile, target);
	}
}
