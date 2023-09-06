package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import isotropy.lmf.core.lang.*;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.model.IFeaturedObject;

import java.util.List;

public class TypeResolutionUtil
{
	public static List<ClassName> toParameters(final List<? extends Concept<?>> parameters)
	{
		return parameters.stream().map(p -> ClassName.get("", p.name())).toList();
	}

	static TypeParameter resolveInclude(final Reference<?> refInclude, final Group<?> group)
	{
		if (refInclude != null)
		{
			final var params = toParameters(refInclude.parameters());
			final var refIncludeGroup = refInclude.group();
			final var model = (Model) refIncludeGroup.lmContainer();
			final var className = ClassName.get(model.domain(), refIncludeGroup.name());
			return TypeParameter.of(className, params);
		}
		else if (group.name().equals("LMObject"))
		{
			final var res = ClassName.get(IFeaturedObject.class);
			return TypeParameter.of(res);
		}
		else
		{
			final var res = ClassName.get(LMObject.class);
			return TypeParameter.of(res);
		}
	}

	public static TypeParameter parametrizedType(Group<?> group, List<? extends Concept<?>> parameters)
	{
		final var model = (Model) group.lmContainer();
		final var className = ClassName.get(model.domain(), group.name());
		if (!parameters.isEmpty())
		{
			final var params = toParameters(parameters);
			return TypeParameter.of(className, params);
		}
		else if (!group.generics()
					   .isEmpty())
		{
			final var params = group.generics()
									.stream()
									.map(g -> ClassName.get("", "?"))
									.toList();
			return TypeParameter.of(className, params);
		}
		else
		{
			return TypeParameter.of(className);
		}
	}

	public static TypeParameter resolveType(final Feature<?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?> attribute)
		{
			final var datatype = attribute.datatype();
			if (datatype instanceof Unit<?> unit)
			{
				final var primitiveType = GenUtils.resolvePrimitiveClass(unit.primitive());
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
					final var params = toParameters(parameters);
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

	public static TypeParameter effectiveType(final Feature<?, ?> feature, TypeParameter singleType)
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

	public static String resolveTypeHolder(final Type<?> type)
	{
		if (type == null) return null;
		else if (type instanceof Group<?>) return "Groups";
		else if (type instanceof Enum<?>) return "Enums";
		else if (type instanceof Unit<?>) return "Units";
		else if (type instanceof JavaWrapper<?>) return "JavaWrappers";
		else return null;
	}

	public static String resolveConceptHolder(final Concept<?> concept)
	{
		if (concept == null) return null;
		else if (concept instanceof Group<?>) return "Groups";
		else if (concept instanceof Generic<?>) return "Generics";
		else return null;
	}
}
