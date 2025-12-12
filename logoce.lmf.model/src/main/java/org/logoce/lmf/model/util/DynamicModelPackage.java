package org.logoce.lmf.model.util;

import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Feature;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Lightweight {@link IModelPackage} implementation backed directly by a {@link MetaModel}.
 * <p>
 * This is intended for tooling/LSP scenarios where no generated {@code *Package}
 * class is available. It provides minimal, map-backed
 * {@link LMObject} instances with containers wired via LMCore {@link Relation}
 * metadata. Only the parts of the runtime needed by the linker and navigation
 * helpers are implemented.
 */
public final class DynamicModelPackage implements IModelPackage
{
	private final AtomicReference<MetaModel> metaModelRef;

	public DynamicModelPackage(final MetaModel metaModel)
	{
		this.metaModelRef = new AtomicReference<>(Objects.requireNonNull(metaModel, "metaModel"));
	}

	private DynamicModelPackage()
	{
		this.metaModelRef = new AtomicReference<>();
	}

	public static DynamicModelPackage unbound()
	{
		return new DynamicModelPackage();
	}

	public void bind(final MetaModel metaModel)
	{
		final var candidate = Objects.requireNonNull(metaModel, "metaModel");
		final var existing = metaModelRef.get();

		if (existing == null)
		{
			metaModelRef.compareAndSet(null, candidate);
			return;
		}

		if (existing != candidate)
		{
			throw new IllegalStateException("DynamicModelPackage already bound to " +
											existing.domain() +
											"." +
											existing.name());
		}
	}

	@Override
	public MetaModel model()
	{
		final var metaModel = metaModelRef.get();
		if (metaModel == null)
		{
			throw new IllegalStateException("DynamicModelPackage is not bound to a MetaModel");
		}
		return metaModel;
	}

	@Override
	public <T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(final Group<T> group)
	{
		if (group == null) return Optional.empty();

		final var metaModel = metaModelRef.get();
		if (metaModel == null)
		{
			return Optional.empty();
		}

		// Only groups from this meta-model are supported; foreign groups fall back
		// to the caller's existing packages (for example LMCorePackage).
		final boolean belongsToMetaModel = metaModel.groups().contains(group);
		if (!belongsToMetaModel)
		{
			return Optional.empty();
		}

		return Optional.of(new DynamicBuilder<>(group));
	}

	@Override
	public <T> Optional<T> resolveEnumLiteral(final Enum<T> anEnum, final String word)
	{
		if (anEnum == null || word == null) return Optional.empty();

		// Dynamic packages do not have access to a concrete Java enum type.
		// Instead, they treat literals as strings and return the matching
		// literal cast to the expected type when possible.
		for (final String literal : anEnum.literals())
		{
			if (literal.equals(word))
			{
				@SuppressWarnings("unchecked")
				final T value = (T) literal;
				return Optional.of(value);
			}
		}

		return Optional.empty();
	}

	/**
	 * Simple builder that creates map-backed LMObjects for a given group.
	 */
	private static final class DynamicBuilder<T extends LMObject> implements IFeaturedObject.Builder<T>
	{
		private final Group<T> group;
		private final Map<Feature<?, ?, ?, ?>, Object> values = new IdentityHashMap<>();
		private final List<ChildRelation> pendingChildren = new ArrayList<>();

		private DynamicBuilder(final Group<T> group)
		{
			this.group = Objects.requireNonNull(group, "group");
		}

		@Override
		public T build()
		{
			final var object = new DynamicFeaturedObject(group, values);

			for (final ChildRelation relation : pendingChildren)
			{
				relation.attachTo(object);
			}

			@SuppressWarnings("unchecked")
			final T result = (T) object;
			return result;
		}

		@Override
		public <AttributeType> void push(final Attribute<?, ?, ?, ?> feature, final AttributeType value)
		{
			if (feature == null) return;

			if (feature.many())
			{
				@SuppressWarnings("unchecked")
				final List<AttributeType> list = (List<AttributeType>) values.computeIfAbsent(feature,
																							  k -> new ArrayList<>());
				if (value != null)
				{
					list.add(value);
				}
			}
			else
			{
				values.put(feature, value);
			}
		}

		@Override
		public <RelationType extends LMObject> void push(final Relation<RelationType, ?, ?, ?> relation,
														final Supplier<RelationType> supplier)
		{
			if (relation == null || supplier == null) return;

			if (relation.many())
			{
				@SuppressWarnings("unchecked")
				final List<RelationType> list = (List<RelationType>) values.computeIfAbsent(relation,
																							 k -> new ArrayList<>());
				final RelationType child = supplier.get();
				if (child != null)
				{
					list.add(child);
					if (relation.contains())
					{
						pendingChildren.add(ChildRelation.many(relation, child));
					}
				}
			}
			else
			{
				final RelationType child = supplier.get();
				values.put(relation, child);
				if (relation.contains() && child != null)
				{
					pendingChildren.add(ChildRelation.single(relation, child));
				}
			}
		}
	}

		private static final class ChildRelation
		{
			private final Relation<?, ?, ?, ?> relation;
			private final LMObject child;
		private final boolean many;

		private ChildRelation(final Relation<?, ?, ?, ?> relation, final LMObject child, final boolean many)
		{
			this.relation = relation;
			this.child = child;
			this.many = many;
		}

		static ChildRelation single(final Relation<?, ?, ?, ?> relation, final LMObject child)
		{
			return new ChildRelation(relation, child, false);
		}

		static ChildRelation many(final Relation<?, ?, ?, ?> relation, final LMObject child)
		{
			return new ChildRelation(relation, child, true);
		}

		void attachTo(final DynamicFeaturedObject parent)
		{
			parent.attachContainment(relation, child, many);
		}
	}
}
