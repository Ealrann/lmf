package org.logoce.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.*;

import java.util.List;

public class TypeResolutionUtil
{
	public static List<ClassName> toParameters(final List<? extends LMEntity<?>> parameters)
	{
		return parameters.stream()
						 .map(p -> ClassName.get("", p.name()))
						 .toList();
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

	public static TypeParameter parametrizedType(Group<?> group, List<? extends LMEntity<?>> parameters)
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
		switch (type)
		{
			case null -> throw new IllegalArgumentException("type cannot be null");
			case Group<?> group ->
			{
				return parametrizedType(group, List.of());
			}
			case Enum<?> enumeration ->
			{
				final var model = (MetaModel) enumeration.lmContainer();
				final var className = ClassName.get(model.domain(), enumeration.name());
				return TypeParameter.of(className);
			}
			case Unit<?> unit ->
			{
				final var primitiveClass = GenUtils.resolvePrimitiveClass(unit.primitive());
				final var typeName = TypeName.get(primitiveClass);
				return TypeParameter.ofPrimitive(typeName);
			}
			case JavaWrapper<?> wrapper ->
			{
				final var qualifiedName = wrapper.qualifiedClassName();
				final var className = ClassName.bestGuess(qualifiedName);
				final var genericCount = GenUtils.genericCount(qualifiedName);
				if (genericCount != 0)
				{
					return TypeParameter.of(className, genericCount);
				}
				else
				{
					return TypeParameter.of(className);
				}
			}
			default -> throw new IllegalStateException("Unsupported type kind: " + type.getClass().getName());
		}

	}

	public static String resolveTypeHolder(final Type<?> type)
	{
		return switch (type)
		{
			case Group<?> _ -> "Groups";
			case Enum<?> _ -> "Enums";
			case Unit<?> _ -> "Units";
			case JavaWrapper<?> _ -> "JavaWrappers";
			default -> null;
		};
	}

	public static String resolveConceptHolder(final Concept<?> concept)
	{
		return switch (concept)
		{
			case Group<?> _ -> "Groups";
			case Generic<?> _ -> "Generics";
			default -> null;
		};
	}
}
