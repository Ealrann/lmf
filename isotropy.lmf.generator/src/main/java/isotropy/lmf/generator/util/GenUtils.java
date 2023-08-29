package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Concept;

import java.util.List;

public final class GenUtils
{
	public static ClassName[] toParameters(final List<? extends Concept<?>> parameters)
	{
		return parameters.stream()
						 .map(p -> ClassName.get("", p.name()))
						 .toArray(ClassName[]::new);
	}

	public static TypeName parameterize(final ClassName className, final List<? extends TypeName> parameters)
	{
		if (parameters.isEmpty())
		{
			return className;
		}
		else
		{
			final TypeName[] array = parameters.toArray(new TypeName[0]);
			return ParameterizedTypeName.get(className, array);
		}
	}

	public static String capitalizeFirstLetter(String str)
	{
		if (str == null || str.isEmpty())
		{
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}
