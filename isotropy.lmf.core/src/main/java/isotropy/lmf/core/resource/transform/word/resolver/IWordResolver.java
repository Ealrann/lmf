package isotropy.lmf.core.resource.transform.word.resolver;

import isotropy.lmf.core.lang.Attribute;
import isotropy.lmf.core.lang.Enum;
import isotropy.lmf.core.lang.Feature;
import isotropy.lmf.core.lang.Relation;
import isotropy.lmf.core.resource.transform.node.TreeBuilderNode;
import isotropy.lmf.core.resource.transform.word.IFeatureResolution;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface IWordResolver<T>
{
	boolean match(String featureName);
	default Optional<? extends IFeatureResolution> resolveOrThrow(TreeBuilderNode<?> node, String value)
	{
		final var res = resolve(node, value);
		if(res.isEmpty())
		{
			throw new NoSuchElementException();
		}
		return res;
	}

	Optional<? extends IFeatureResolution> resolve(TreeBuilderNode<?> node, String value);

	boolean isBooleanAttribute();


	static Optional<IWordResolver<?>> buildResolver(Feature<?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?> attribute)
		{
			if (attribute.datatype() instanceof Enum<?>)
			{
				return Optional.of(new EnumResolver<>((Attribute<?, ?>) feature));
			}
			else
			{
				return Optional.of(new UnitResolver<>((Attribute<?, ?>) feature));
			}
		}
		else
		{
			return buildRelationResolver((Relation<?, ?>) feature);
		}
	}

	static Optional<IWordResolver<?>> buildRelationResolver(Relation<?, ?> relation)
	{
		if (!relation.contains())
		{
			return Optional.of(new ReferenceResolver<>(relation));
		}
		else
		{
			return Optional.empty();
		}
	}
}
