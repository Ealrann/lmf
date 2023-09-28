package org.logoce.lmf.model.resource.transform.word.resolver;

import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.transform.node.TreeBuilderNode;
import org.logoce.lmf.model.resource.transform.word.IFeatureResolution;

import java.util.Optional;

public interface IWordResolver<T>
{
	boolean match(String featureName);
	IFeatureResolution resolveOrThrow(TreeBuilderNode<?> node, String value);
	Optional<? extends IFeatureResolution> resolve(TreeBuilderNode<?> node, String value);

	@SuppressWarnings("unchecked")
	static Optional<IWordResolver<?>> buildResolver(Feature<?, ?> feature)
	{
		if (feature instanceof Attribute<?, ?> attribute)
		{
			if (attribute.datatype() instanceof Enum<?>)
			{
				return Optional.of(new EnumResolver<>((Attribute<?, ?>) feature));
			}
			else if (attribute.datatype() instanceof Unit<?>)
			{
				return Optional.of(new UnitResolver<>((Attribute<?, ?>) feature));
			}
			else
			{
				return Optional.of(new JavaWrapperResolver((Attribute<Object, ?>) feature));
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
