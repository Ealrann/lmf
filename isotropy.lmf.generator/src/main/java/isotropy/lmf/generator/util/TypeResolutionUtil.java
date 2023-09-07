package isotropy.lmf.generator.util;

import com.squareup.javapoet.ClassName;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.*;
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
