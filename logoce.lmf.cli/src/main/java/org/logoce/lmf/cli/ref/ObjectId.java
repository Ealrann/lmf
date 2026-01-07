package org.logoce.lmf.cli.ref;

import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stable identifier for a semantic object, based on its owning model qualified name
 * and its containment path within that model.
 */
public record ObjectId(String modelQualifiedName, String path)
{
	public static ObjectId from(final LMObject object)
	{
		if (object == null)
		{
			return null;
		}

		final var segments = new ArrayList<String>();
		LMObject cursor = object;

		while (cursor.lmContainer() != null)
		{
			final var parent = cursor.lmContainer();
			final Feature<?, ?, ?, ?> feature = cursor.lmContainingFeature();
			if (feature == null)
			{
				break;
			}

			final String featureName = feature.name();
			if (feature.many())
			{
				final var list = (List<?>) parent.get(feature);
				final int index = list.indexOf(cursor);
				segments.add(featureName + "." + index);
			}
			else
			{
				segments.add(featureName);
			}

			cursor = parent;
		}

		final var root = cursor;
		if (!(root instanceof Model model))
		{
			return null;
		}

		final String domain = model.domain();
		final String name = model.name();
		final String qualifiedName = domain == null || domain.isBlank() ? name : domain + "." + name;

		if (segments.isEmpty())
		{
			return new ObjectId(qualifiedName, "/");
		}

		java.util.Collections.reverse(segments);
		return new ObjectId(qualifiedName, "/" + String.join("/", segments));
	}
}

