package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.resource.linking.FeatureLink;
import org.logoce.lmf.model.resource.linking.tree.LinkNode;

import java.util.List;
import java.util.Optional;

public interface ITokenResolver<T>
{
	boolean match(String featureName);
	Optional<? extends FeatureLink> resolve(LinkNode.Structure<?> node, List<String> value);

	@SuppressWarnings("unchecked")
	static Optional<ITokenResolver<?>> buildResolver(Feature<?, ?> feature)
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

	static Optional<ITokenResolver<?>> buildRelationResolver(Relation<?, ?> relation)
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
