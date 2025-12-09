package org.logoce.lmf.model.util;

import org.logoce.lmf.model.api.model.AdaptableStructureObject;
import org.logoce.lmf.model.api.model.IFeaturedObject;
import org.logoce.lmf.model.api.model.IModelPackage;
import org.logoce.lmf.model.lang.Attribute;
import org.logoce.lmf.model.lang.Enum;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Named;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.lang.Feature;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Lightweight {@link IModelPackage} implementation backed directly by a {@link MetaModel}.
 * <p>
 * This is intended for tooling/LSP scenarios where no generated {@code *Package}
 * class is available on the classpath. It provides minimal, map-backed
 * {@link LMObject} instances with containers wired via LMCore {@link Relation}
 * metadata. Only the parts of the runtime needed by the linker and navigation
 * helpers are implemented.
 */
public final class DynamicModelPackage implements IModelPackage
{
	private final MetaModel metaModel;

	public DynamicModelPackage(final MetaModel metaModel)
	{
		this.metaModel = Objects.requireNonNull(metaModel, "metaModel");
	}

	@Override
	public MetaModel model()
	{
		return metaModel;
	}

	@Override
	public <T extends LMObject> Optional<IFeaturedObject.Builder<T>> builder(final Group<T> group)
	{
		if (group == null) return Optional.empty();

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
		private final Map<Feature<?, ?>, Object> values = new HashMap<>();
		private final List<ChildRelation> pendingChildren = new ArrayList<>();

		private DynamicBuilder(final Group<T> group)
		{
			this.group = Objects.requireNonNull(group, "group");
		}

		@Override
		public T build()
		{
			final var object = new DynamicLMObject<>(group, values);

			for (final ChildRelation relation : pendingChildren)
			{
				relation.attachTo(object);
			}

			@SuppressWarnings("unchecked")
			final T result = (T) object;
			return result;
		}

		@Override
		public <AttributeType> void push(final Attribute<AttributeType, ?> feature, final AttributeType value)
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
		public <RelationType extends LMObject> void push(final Relation<RelationType, ?> relation,
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
		private final Relation<?, ?> relation;
		private final LMObject child;
		private final boolean many;

		private ChildRelation(final Relation<?, ?> relation, final LMObject child, final boolean many)
		{
			this.relation = relation;
			this.child = child;
			this.many = many;
		}

		static ChildRelation single(final Relation<?, ?> relation, final LMObject child)
		{
			return new ChildRelation(relation, child, false);
		}

		static ChildRelation many(final Relation<?, ?> relation, final LMObject child)
		{
			return new ChildRelation(relation, child, true);
		}

		void attachTo(final DynamicLMObject<?> parent)
		{
			parent.setContainer(child, relation, many);
		}
	}

	/**
	 * Minimal LMObject implementation that stores feature values in a map.
	 */
	private static final class DynamicLMObject<T extends LMObject> extends AdaptableStructureObject
		implements LMObject, Model, Named
	{
		private final Group<T> group;
		private final Map<Feature<?, ?>, Object> values;
		private LMObject container;
		private Relation<?, ?> containingFeature;

		private DynamicLMObject(final Group<T> group, final Map<Feature<?, ?>, Object> initialValues)
		{
			this.group = Objects.requireNonNull(group, "group");
			this.values = new HashMap<>(initialValues);
		}

		@Override
		public Group<T> lmGroup()
		{
			return group;
		}

		@Override
		public String name()
		{
			final Attribute<String, ?> nameAttribute = findAttribute("name");
			return nameAttribute == null ? null : (String) get(nameAttribute);
		}

		@Override
		public String domain()
		{
			final Attribute<String, ?> domainAttribute = findAttribute("domain");
			return domainAttribute == null ? null : (String) get(domainAttribute);
		}

		@SuppressWarnings("unchecked")
		@Override
		public java.util.List<String> imports()
		{
			final Attribute<java.util.List<String>, ?> importsAttribute = findAttribute("imports");
			return importsAttribute == null ? java.util.List.of()
											: (java.util.List<String>) get(importsAttribute);
		}

		@SuppressWarnings("unchecked")
		@Override
		public java.util.List<String> metamodels()
		{
			final Attribute<java.util.List<String>, ?> metamodelsAttribute = findAttribute("metamodels");
			return metamodelsAttribute == null ? java.util.List.of()
											   : (java.util.List<String>) get(metamodelsAttribute);
		}

		@Override
		public LMObject lmContainer()
		{
			return container;
		}

		@Override
		public Relation<?, ?> lmContainingFeature()
		{
			return containingFeature;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V> V get(final Feature<?, V> feature)
		{
			if (feature == null) return null;

			if (feature.many())
			{
				@SuppressWarnings("unchecked")
				final V list = (V) values.computeIfAbsent(feature, k -> new ArrayList<>());
				return list;
			}

			@SuppressWarnings("unchecked")
			final V value = (V) values.get(feature);
			return value;
		}

		@Override
		public <V> void set(final Feature<?, V> feature, final V value)
		{
			if (feature == null) return;

			values.put(feature, value);
		}

		@SuppressWarnings("unchecked")
		private <V> Attribute<V, ?> findAttribute(final String attributeName)
		{
			if (attributeName == null) return null;

			for (final Feature<?, ?> feature : group.features())
			{
				if (feature instanceof Attribute<?, ?> attribute && attributeName.equals(attribute.name()))
				{
					return (Attribute<V, ?>) attribute;
				}
			}
			return null;
		}

		@Override
		protected int featureIndex(final int featureId)
		{
			return 0;
		}

		@Override
		public void listenStruture(final Consumer<org.logoce.lmf.model.api.notification.Notification> listener)
		{
			// Dynamic objects are not expected to participate in structure notifications
			// in tooling scenarios; use the base EMF-style listener APIs instead.
		}

		@Override
		public void sulkStructure(final Consumer<org.logoce.lmf.model.api.notification.Notification> listener)
		{
			// See listenStruture.
		}

		private void setContainer(final LMObject child, final Relation<?, ?> relation, final boolean many)
		{
			if (!(child instanceof DynamicLMObject<?> dynamicChild)) return;

			dynamicChild.container = this;
			dynamicChild.containingFeature = relation;

			if (many)
			{
				@SuppressWarnings("unchecked")
				final List<LMObject> list = (List<LMObject>) values.computeIfAbsent(relation,
																					k -> new ArrayList<>());
				if (!list.contains(child))
				{
					list.add(child);
				}
			}
			else
			{
				values.put(relation, child);
			}
		}
	}
}
