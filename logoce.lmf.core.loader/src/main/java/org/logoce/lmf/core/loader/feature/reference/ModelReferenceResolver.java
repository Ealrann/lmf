package org.logoce.lmf.core.loader.feature.reference;

import org.logoce.lmf.core.api.model.IFeaturedObject;
import org.logoce.lmf.core.lang.Group;
import org.logoce.lmf.core.lang.LMObject;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.lang.Named;
import org.logoce.lmf.core.lang.Relation;
import org.logoce.lmf.core.loader.api.loader.linking.FeatureResolution;
import org.logoce.lmf.core.loader.api.loader.linking.AmbiguousReferenceException;
import org.logoce.lmf.core.loader.api.loader.linking.InvalidReferenceException;
import org.logoce.lmf.core.api.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class ModelReferenceResolver implements ReferenceResolver
{
	private final Model model;
	private final Relation<?, ?, ?, ?> relation;

	/**
	 * Marker interface for static model reference resolutions that point to a
	 * concrete target {@link LMObject}. This is used by tooling-oriented
	 * consumers (for example the semantic index builder) to extract the target
	 * object without depending on the internal {@link ModelExplorer} type.
	 */
	public interface StaticResolution extends FeatureResolution<Relation<?, ?, ?, ?>>
	{
		LMObject value();
	}

	public ModelReferenceResolver(final Model model, final Relation<?, ?, ?, ?> relation)
	{
		this.model = model;
		this.relation = relation;
	}

	@Override
	public Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolve(final PathParser pathParser)
	{
		final var rawReference = pathParser.rawPath();
		final var firstStep = pathParser.next();
		if (!pathParser.hasNext())
		{
			if (firstStep.type() == PathParser.Type.NAME)
			{
				return resolveName(rawReference, firstStep.text(), relation);
			}
			if (firstStep.type() == PathParser.Type.CHILD)
			{
				final var explorer = new ModelExplorer(model);
				explorer.apply(firstStep);
				return Optional.of(explorer.buildOrThrow(relation, rawReference));
			}
			throw new NoSuchElementException("Unsupported first step in model reference: " + firstStep.type());
		}

		final var remaining = collectRemaining(pathParser);

		if (firstStep.type() == PathParser.Type.NAME)
		{
			return resolveNamedPath(rawReference, firstStep.text(), remaining, relation);
		}
		else
		{
			final var explorer = new ModelExplorer(model);
			explorer.apply(firstStep);
			for (final var step : remaining)
			{
				explorer.apply(step);
			}
			return Optional.of(explorer.buildOrThrow(relation, rawReference));
		}
	}

	private static List<PathParser.Step> collectRemaining(final PathParser parser)
	{
		final var steps = new ArrayList<PathParser.Step>();
		while (parser.hasNext())
		{
			steps.add(parser.next());
		}
		return List.copyOf(steps);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolveName(final String rawReference,
																							 final String name,
																							 final Relation<T, ?, ?, ?> relation)
	{
		final var concept = relation.concept();
		final var expectedGroup = concept instanceof Group<?> group ? group : null;
		final var expectedName = concept == null ? null : concept.name();

		final var candidates = ModelUtil.streamTree(model)
										.filter(o -> o instanceof Named named && name.equals(named.name()))
										.toList();
		final var matches = expectedGroup == null
							? candidates
							: candidates.stream().filter(o -> ModelUtil.isSubGroup(expectedGroup, o.lmGroup())).toList();
		if (!matches.isEmpty())
		{
			final var object = (T) matches.getFirst();
			return Optional.of(new ModelExplorer.StaticReferenceResolution<>(relation, object));
		}

		if (candidates.size() == 1)
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedName,
												candidates.getFirst().lmGroup().name());
		}
		throw new InvalidReferenceException(relation.name(), rawReference, expectedName);
	}

	@SuppressWarnings("unchecked")
	private <T extends LMObject> Optional<FeatureResolution<Relation<?, ?, ?, ?>>> resolveNamedPath(final String rawReference,
																									final String name,
																									final List<PathParser.Step> remaining,
																									final Relation<T, ?, ?, ?> relation)
	{
		final var concept = relation.concept();
		final var expectedGroup = concept instanceof Group<?> group ? group : null;
		final var expectedName = concept == null ? null : concept.name();

		final var candidates = ModelUtil.streamTree(model)
										.filter(o -> o instanceof Named named && name.equals(named.name()))
										.toList();
		final var resolved = candidates.stream()
									   .map(anchor -> tryResolveFrom(anchor, remaining))
									   .filter(Optional::isPresent)
									   .map(Optional::get)
									   .toList();
		final var matches = expectedGroup == null
							? resolved
							: resolved.stream().filter(o -> ModelUtil.isSubGroup(expectedGroup, o.lmGroup())).toList();

		if (matches.size() == 1)
		{
			final var object = (T) matches.getFirst();
			return Optional.of(new ModelExplorer.StaticReferenceResolution<>(relation, object));
		}
		if (matches.size() > 1)
		{
			throw new AmbiguousReferenceException(relation.name(),
												  rawReference,
												  expectedName,
												  matches.size());
		}
		if (resolved.size() == 1)
		{
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expectedName,
												resolved.getFirst().lmGroup().name());
		}
		throw new InvalidReferenceException(relation.name(), rawReference, expectedName);
	}

	private static Optional<LMObject> tryResolveFrom(final LMObject anchor, final List<PathParser.Step> remaining)
	{
		try
		{
			final var explorer = new ModelExplorer(anchor);
			for (final var step : remaining)
			{
				explorer.apply(step);
			}
			return Optional.of(explorer.current());
		}
		catch (final NoSuchElementException exception)
		{
			return Optional.empty();
		}
	}

	private static final class ModelExplorer
	{
		private LMObject current;

		ModelExplorer(final LMObject start)
		{
			this.current = start;
		}

		LMObject current()
		{
			return current;
		}

		void apply(final PathParser.Step step)
		{
			current = switch (step.type())
			{
				case CHILD -> exploreChild(current, step.text());
				case PARENT -> requireParent(current);
				case CURRENT -> current;
				case NAME -> exploreName(step.text());
				case CONTEXT_NAME -> exploreContextName(step.text());
				case ROOT -> ModelUtil.root(current);
				default -> throw new NoSuchElementException("Unsupported step in model path: " + step.type());
			};
		}

		private static LMObject requireParent(final LMObject object)
		{
			final var parent = object.lmContainer();
			if (parent == null)
			{
				throw new NoSuchElementException("Cannot resolve parent of root object");
			}
			return parent;
		}

		private LMObject exploreName(final String name)
		{
			return ModelUtil.streamTree(ModelUtil.root(current))
							.filter(o -> o instanceof Named named && name.equals(named.name()))
							.findAny()
							.orElseThrow(() -> new NoSuchElementException("Cannot find named Object : " + name));
		}

		private LMObject exploreContextName(final String name)
		{
			LMObject cursor = current;
			while (cursor != null)
			{
				if (cursor instanceof Named named && name.equals(named.name()))
				{
					return cursor;
				}

				final var matchingChild = cursor.streamChildren()
												.filter(o -> o instanceof Named named && name.equals(named.name()))
												.findAny();
				if (matchingChild.isPresent())
				{
					return matchingChild.get();
				}

				cursor = cursor.lmContainer();
			}
			throw new NoSuchElementException("Cannot find context-named Object : " + name);
		}

		@SuppressWarnings("unchecked")
		private static LMObject exploreChild(final LMObject current, final String step)
		{
			LMObject cursor = current;
			final var pointIndex = step.indexOf('.');
			final var featureName = pointIndex == -1 ? step : step.substring(0, pointIndex);
			final Integer index = pointIndex == -1 ? null : Integer.valueOf(step.substring(pointIndex + 1));

			final var feature = cursor.lmGroup()
									   .features()
									   .stream()
									   .filter(f -> f.name().equals(featureName))
									   .findAny()
									   .orElseThrow(() -> new NoSuchElementException("Cannot resolve step " + step));

			if (index != null)
			{
				@SuppressWarnings("unchecked")
				final var featureValue = (List<? extends LMObject>) cursor.get(
						(Relation<?, List<? extends LMObject>, ?, ?>) feature);
				cursor = featureValue.get(index);
			}
			else
			{
				final var featureValue = (LMObject) cursor.get(
						(Relation<?, ? extends LMObject, ?, ?>) feature);
				cursor = featureValue;
			}

			if (cursor == null)
			{
				throw new NoSuchElementException("Cannot resolve step " + step);
			}

			return cursor;
		}

		@SuppressWarnings("unchecked")
		public <T extends Relation<?, ?, ?, ?>> FeatureResolution<Relation<?, ?, ?, ?>> buildOrThrow(final T relation,
																									 final String rawReference)
		{
			final var concept = relation.concept();
			if (concept instanceof Group<?> group && ModelUtil.isSubGroup(group, current.lmGroup()))
			{
				return buildInternal(relation, current);
			}

			final var expected = concept == null ? null : concept.name();
			throw new InvalidReferenceException(relation.name(),
												rawReference,
												expected,
												current.lmGroup().name());
		}

		private static <Y extends LMObject> StaticReferenceResolution<Y, Relation<Y, ?, ?, ?>> buildInternal(final Relation<?, ?, ?, ?> relation,
																									   final Y object)
		{
			@SuppressWarnings("unchecked")
			final var typedRelation = (Relation<Y, ?, ?, ?>) relation;
			return new StaticReferenceResolution<>(typedRelation, object);
		}

		public record StaticReferenceResolution<Y extends LMObject, T extends Relation<Y, ?, ?, ?>>(T relation,
																									Y value) implements
																									   FeatureResolution<Relation<?, ?, ?, ?>>,
																									   StaticResolution
		{
			@Override
			public void pushValue(final IFeaturedObject.Builder<?> builder)
			{
				builder.push(relation, () -> value);
			}

			@Override
			public T feature()
			{
				return relation;
			}
		}
	}
}
