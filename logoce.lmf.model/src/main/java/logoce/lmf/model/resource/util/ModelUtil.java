package logoce.lmf.model.resource.util;

import logoce.lmf.model.lang.Relation;
import logoce.lmf.model.lang.LMObject;

import java.util.List;
import java.util.stream.Stream;

public class ModelUtil
{
	@SuppressWarnings("unchecked")
	public static final Stream<LMObject> streamTree(final LMObject root)
	{
		final var childStream = root.lmGroup()
									.features()
									.stream()
									.filter(Relation.class::isInstance)
									.map(Relation.class::cast)
									.filter(Relation::contains)
									.flatMap(r -> streamChildren(root, r));

		return Stream.concat(Stream.of(root), childStream);
	}

	@SuppressWarnings("unchecked")
	public static final <T extends LMObject> Stream<T> streamChildren(final LMObject element,
																	  final Relation<T, ?> relation)
	{
		if (relation.many())
		{
			final var list = (List<T>) element.get(relation);
			return list.stream();
		}
		else
		{
			return Stream.of((T) element.get(relation));
		}
	}
}
