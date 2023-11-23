package org.logoce.lmf.model.resource.linking.feature;

import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.*;

import java.util.Optional;

public interface ITokenResolver
{
	boolean match(String featureName);

	static Optional<RelationResolver> buildRelationResolver(final Relation<?, ?> relation)
	{
		if (!relation.contains())
		{
			return Optional.of(new RelationResolver(relation));
		}
		else
		{
			return Optional.empty();
		}
	}

	static Optional<AttributeResolver> buildAttributeResolver(final Attribute<?, ?> attribute)
	{
		if (attribute.datatype() instanceof Enum<?>)
		{
			return Optional.of(new EnumResolver<>(attribute));
		}
		else if (attribute.datatype() instanceof Unit<?>)
		{
			return Optional.of(new UnitResolver<>(attribute));
		}
		else
		{
			return Optional.of(new JavaWrapperResolver(attribute));
		}
	}
}
