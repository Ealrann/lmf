package isotropy.lmf.generator.group.feature;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.generator.util.GenUtils;
import isotropy.lmf.generator.util.TypeParameter;

import java.util.List;

public final class MethodUtil
{
	private static final String PREFIX = "add";

	static TypeParameter effectiveType(final Feature<?, ?> feature, TypeParameter singleType)
	{
		if (feature.many())
		{
			return singleType.nestIn(ClassName.get(List.class));
		}
		else
		{
			return singleType;
		}
	}

	static TypeParameter resolveType(final Feature<?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			if (datatype instanceof Unit<?> unit)
			{
				final var primitiveType = switch (unit.primitive())
				{
					case Boolean -> boolean.class;
					case Int -> int.class;
					case Long -> long.class;
					case Float -> float.class;
					case Double -> double.class;
					case String -> String.class;
				};
				final var typeName = TypeName.get(primitiveType);
				return TypeParameter.of(typeName);
			}
			else if (datatype instanceof Enum<?> enumeration)
			{
				final var model = (Model) enumeration.lmContainer();
				final var className = ClassName.get(model.domain(), enumeration.name());
				return TypeParameter.of(className);
			}
			else
			{
				final var javaWrapper = (JavaWrapper<?>) datatype;
				final var parameters = attribute.parameters();
				final var className = ClassName.get(javaWrapper.domain(), javaWrapper.name());
				if (!parameters.isEmpty())
				{
					final var params = GenUtils.toParameters(parameters);
					return TypeParameter.of(className, params);
				}
				else
				{
					return TypeParameter.of(className);
				}
			}
		}
		else
		{
			final var relation = (Relation<?, ?>) feature;
			final var reference = relation.reference();
			final var concept = reference.group();
			if (concept instanceof Group<?> group)
			{
				return parametrizedType(group, reference.parameters());
			}
			else
			{
				final var generic = (Generic<?>) concept;
				final var className = ClassName.get("", generic.name());
				return TypeParameter.of(className);
			}
		}
	}

	private static TypeParameter parametrizedType(Group<?> group, List<? extends Concept<?>> parameters)
	{
		final var model = (Model) group.lmContainer();
		final var className = ClassName.get(model.domain(), group.name());
		if (!parameters.isEmpty())
		{
			final var params = GenUtils.toParameters(parameters);
			return TypeParameter.of(className, params);
		}
		else if (!group.generics()
					   .isEmpty())
		{
			final var params = group.generics()
									.stream()
									.map(g -> ClassName.get("", "?"))
									.toArray(ClassName[]::new);
			return TypeParameter.of(className, params);
		}
		else
		{
			return TypeParameter.of(className);
		}
	}

	public static String builderMethodName(final FeatureResolution f)
	{
		return f.feature()
				.many() ? "add" + GenUtils.capitalizeFirstLetter(f.name()) : f.name();
	}
}
