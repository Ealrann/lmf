package org.logoce.lmf.core.loader.linking.feature;

import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Enum;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.Unit;
import org.logoce.lmf.core.util.ModelRegistry;

import java.util.Optional;

public interface ITokenResolver
{
	boolean match(String featureName);

	final class Builder
	{
		private final ModelRegistry registry;

		public Builder(final ModelRegistry registry)
		{
			this.registry = registry;
		}

		public Optional<RelationResolver> buildRelationResolver(final Relation<?, ?, ?, ?> relation)
		{
			if (!relation.contains())
			{
				return Optional.of(new RelationResolver(relation, registry));
			}
			else
			{
				return Optional.empty();
			}
		}

		public static Optional<AttributeResolver> buildAttributeResolver(final Attribute<?, ?, ?, ?> attribute)
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
}
