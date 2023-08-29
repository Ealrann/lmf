package isotropy.lmf.generator.model;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.generator.util.GenUtils;

import javax.lang.model.element.Modifier;
import java.util.List;

public interface MethodUtil
{

	final class MethodBuilder
	{
		public MethodSpec build(Feature<?, ?> feature)
		{
			final var featureName = feature.name();
			final var rawType = resolveType(feature);
			final var effectiveType = resolveEffectiveType(feature, rawType);
			final var featureMethod = MethodSpec.methodBuilder(featureName)
												.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
												.returns(effectiveType);

			return featureMethod.build();
		}
	}

	final class BuilderMethodBuilder
	{
		private final TypeName typedBuilder;

		public BuilderMethodBuilder(TypeName typedBuilder)
		{
			this.typedBuilder = typedBuilder;
		}

		public MethodSpec build(Feature<?, ?> feature)
		{
			final var featureName = feature.name();
			final var name = feature.many() ? "add" + GenUtils.capitalizeFirstLetter(featureName) : featureName;
			final var rawType = resolveType(feature);
			final var builderFeatureMethod = MethodSpec.methodBuilder(name)
													   .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
													   .returns(typedBuilder)
													   .addParameter(rawType, featureName);

			return builderFeatureMethod.build();
		}
	}

	private static TypeName resolveEffectiveType(final Feature<?, ?> feature, TypeName rawType)
	{
		if (feature.many())
		{
			final var typeList = ClassName.get(List.class);
			return ParameterizedTypeName.get(typeList, rawType);
		}
		else
		{
			return rawType;
		}
	}

	private static TypeName resolveType(final Feature<?, ?> feature)
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
				return TypeName.get(primitiveType);
			}
			else
			{
				final var enumeration = (Enum<?>) datatype;
				final var model = (Model) enumeration.lmContainer();
				return ClassName.get(model.domain(), enumeration.name());
			}
		}
		else
		{
			final var relation = (Relation<?, ?>) feature;
			final var reference = relation.reference();
			final var concept = reference.group();
			if (concept instanceof Group<?> group)
			{
				return parametrizeType(group, reference.parameters());
			}
			else
			{
				final var generic = (Generic<?>) concept;
				return ClassName.get("", generic.name());
			}
		}
	}

	private static TypeName parametrizeType(Group<?> group, List<? extends Concept<?>> parameters)
	{
		final var model = (Model) group.lmContainer();
		final var className = ClassName.get(model.domain(), group.name());
		if (!parameters.isEmpty())
		{
			final var params = GenUtils.toParameters(parameters);
			return ParameterizedTypeName.get(className, params);
		}
		else if (!group.generics()
					   .isEmpty())
		{
			final var params = group.generics()
									.stream()
									.map(g -> ClassName.get("", "?"))
									.toArray(ClassName[]::new);
			return ParameterizedTypeName.get(className, params);
		}
		else
		{
			return className;
		}
	}
}
