package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.*;

import java.util.List;

public class TypeResolutionUtil
{
	public static List<ClassName> toParameters(final List<? extends Concept<?>> parameters)
	{
		return parameters.stream().map(p -> ClassName.get("", p.name())).toList();
	}

	public static TypeParameter resolveInclude(final Reference<?> refInclude, final Group<?> group)
	{
		final var params = toParameters(refInclude.parameters());
		final var refIncludeGroup = refInclude.group();
		final var model = (MetaModel) refIncludeGroup.lmContainer();
		final var className = ClassName.get(model.domain(), refIncludeGroup.name());
		return TypeParameter.of(className, params);
	}

	public static TypeParameter resolveNoInclude(final Group<?> group)
	{
		if (group.name().equals("LMObject"))
		{
			final var res = ClassName.get(IFeaturedObject.class);
			return TypeParameter.of(res);
		}
		else
		{
			return TypeParameter.of(ConstantTypes.LM_OBJECT);
		}
	}

	public static TypeParameter parametrizedType(Group<?> group, List<? extends Concept<?>> parameters)
	{
		final var model = (MetaModel) group.lmContainer();
		final var className = ClassName.get(model.domain(), group.name());
		if (!parameters.isEmpty())
		{
			final var params = toParameters(parameters);
			return TypeParameter.of(className, params);
		}
		else if (!group.generics().isEmpty())
		{
			final var params = group.generics().stream().map(g -> ClassName.get("", "?")).toList();
			return TypeParameter.of(className, params);
		}
		else
		{
			return TypeParameter.of(className);
		}
	}

	/**
	 * Resolve a simple Type (Group / Datatype / Enum / Unit / JavaWrapper) into a TypeParameter
	 * for use in operation signatures and other non-feature contexts.
	 */
	public static TypeParameter resolveSimpleType(final Type<?> type)
	{
		if (type == null)
		{
			throw new IllegalArgumentException("type cannot be null");
		}

		if (type instanceof Group<?> group)
		{
			return parametrizedType(group, List.of());
		}
		else if (type instanceof org.logoce.lmf.model.lang.Enum<?> enumeration)
		{
			final var model = (MetaModel) enumeration.lmContainer();
			final var className = ClassName.get(model.domain(), enumeration.name());
			return TypeParameter.of(className);
		}
		else if (type instanceof Unit<?> unit)
		{
			final var primitiveClass = GenUtils.resolvePrimitiveClass(unit.primitive());
			final var typeName = TypeName.get(primitiveClass);
			return TypeParameter.ofPrimitive(typeName);
		}
		else if (type instanceof JavaWrapper<?> wrapper)
		{
			final var domain = wrapper.domain();
			final var name = wrapper.name();
			final var className = ClassName.get(domain, name);
			final var genericCount = GenUtils.genericCount(domain + "." + name);
			if (genericCount != 0)
			{
				return TypeParameter.of(className, genericCount);
			}
			else
			{
				return TypeParameter.of(className);
			}
		}
		else
		{
			throw new IllegalStateException("Unsupported type kind: " + type.getClass().getName());
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
