package org.logoce.lmf.core.loader.feature;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.lang.Attribute;
import org.logoce.lmf.core.lang.Feature;
import org.logoce.lmf.core.lang.LMCoreModelDefinition;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.lang.builder.AttributeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public final class UnitExtractorTest
{
	@Test
	void longWithSuffix_usesExtractor()
	{
		assertNotNull(LMCoreModelDefinition.Units.LONG.extractor());

		final var attribute = new AttributeBuilder<Long, Long, Object, Object>()
				.name("aLong")
				.id(1)
				.many(false)
				.mandatory(true)
				.datatype(() -> LMCoreModelDefinition.Units.LONG)
				.build();

		final var resolved = resolveSingleValue(attribute, "4000000L");
		assertEquals(4_000_000L, resolved);
	}

	@Test
	void floatWithSuffix_usesExtractor()
	{
		assertNotNull(LMCoreModelDefinition.Units.FLOAT.extractor());

		final var attribute = new AttributeBuilder<Float, Float, Object, Object>()
				.name("aFloat")
				.id(2)
				.many(false)
				.mandatory(true)
				.datatype(() -> LMCoreModelDefinition.Units.FLOAT)
				.build();

		final var resolved = resolveSingleValue(attribute, "1.5f");
		assertEquals(1.5f, resolved);
	}

	private static <T> T resolveSingleValue(final Attribute<T, ?, ?, ?> attribute, final String rawValue)
	{
		final var resolver = new UnitResolver<>(attribute);
		final var resolution = resolver.resolve(List.of(rawValue)).orElseThrow();

		final var builder = new CapturingBuilder();
		resolution.pushValue(builder);

		@SuppressWarnings("unchecked")
		final var value = (T) builder.values.get(attribute);
		assertNotNull(value, "Resolver did not push a value for " + attribute.name());
		return value;
	}

	private static final class CapturingBuilder implements IFeaturedObject.Builder<LMObject>
	{
		private final Map<Feature<?, ?, ?, ?>, Object> values = new HashMap<>();

		@Override
		public LMObject build()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <AttributeType> void push(final Attribute<?, ?, ?, ?> feature, final AttributeType value)
		{
			values.put(feature, value);
		}

		@Override
		public <RelationType extends LMObject> void push(final Relation<RelationType, ?, ?, ?> relation,
														 final Supplier<RelationType> supplier)
		{
			throw new UnsupportedOperationException();
		}
	}
}

