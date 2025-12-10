package org.logoce.lmf.model.loader.model;

import org.logoce.lmf.model.lang.*;
import org.logoce.lmf.model.loader.linking.LinkNode;
import org.logoce.lmf.model.loader.linking.ResolutionAttempt;
import org.logoce.lmf.model.loader.linking.feature.AttributeResolver;
import org.logoce.lmf.model.loader.linking.feature.RelationResolver;
import org.logoce.lmf.model.loader.linking.feature.reference.ModelReferenceResolver;
import org.logoce.lmf.model.loader.linking.tree.LinkNodeFull;
import org.logoce.lmf.model.resource.interpretation.PFeature;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtil;
import org.logoce.lmf.model.util.TextPositions;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.logoce.lmf.model.loader.model.LmSymbolIndex.ReferenceSpan;
import static org.logoce.lmf.model.loader.model.LmSymbolIndex.SymbolId;
import static org.logoce.lmf.model.loader.model.LmSymbolIndex.SymbolKind;
import static org.logoce.lmf.model.loader.model.LmSymbolIndex.SymbolSpan;

/**
 * Experimental semantic index builder operating on linked LMObjects rather than
 * raw syntax headers.
 * <p>
 * It uses {@link LinkNode} trees and the owning {@link Model} instances from a
 * {@link ModelRegistry} to derive:
 * <ul>
 *   <li>Declarations – {@link LMObject}s classified by LMCore types and mapped
 *       to a {@link SymbolId} with a source span.</li>
 *   <li>References – relation-valued features whose resolved target objects are
 *       known declarations.</li>
 * </ul>
 * The API is intentionally conservative: it focuses on the core patterns needed
 * by tooling and avoids making assumptions about header keywords or LMCore
 * aliases. Callers can gradually adopt this builder alongside the existing
 * {@link LmSymbolIndexBuilder}.
 */
public final class LmSemanticIndexBuilder
{
	private LmSemanticIndexBuilder()
	{
	}

	/**
	 * Build a symbol index from linked objects and link trees.
	 *
	 * @param rootModel the root model for this document (MetaModel or other Model)
	 * @param linkTrees link-tree roots produced by {@code LmModelLinker}
	 * @param registry  model registry used to resolve owning models for objects
	 * @param source    full document text
	 */
	public static LmSymbolIndex buildIndex(final Model rootModel,
										   final List<? extends LinkNode<?, PNode>> linkTrees,
										   final ModelRegistry registry,
										   final CharSequence source)
	{
		if (rootModel == null || linkTrees == null || linkTrees.isEmpty())
		{
			return new LmSymbolIndex(List.of(), List.of());
		}

		// 1) Map each LMObject built from the link trees to its LinkNodeFull.
		final Map<LMObject, LinkNodeFull<?, PNode>> nodeByObject = new IdentityHashMap<>();

		for (final LinkNode<?, PNode> root : linkTrees)
		{
			if (root instanceof LinkNodeFull<?, PNode> fullRoot)
			{
				fullRoot.streamTree().forEach(node -> {
					final LMObject object;
					try
					{
						object = node.build();
					}
					catch (Exception e)
					{
						return;
					}

					if (object != null)
					{
						nodeByObject.putIfAbsent(object, node);
					}
				});
			}
		}

		if (nodeByObject.isEmpty())
		{
			return new LmSymbolIndex(List.of(), List.of());
		}

		// 2) Map LMObjects to their owning Models using the registry (and the root model).
		final Map<LMObject, Model> owningModel = new IdentityHashMap<>();
		registry.models().forEach(model -> {
			for (final LMObject object : (Iterable<LMObject>) () -> ModelUtil.streamTree(model).iterator())
			{
				owningModel.putIfAbsent(object, model);
			}
		});

		if (!owningModel.containsKey(rootModel))
		{
			for (final LMObject object : (Iterable<LMObject>) () -> ModelUtil.streamTree(rootModel).iterator())
			{
				owningModel.putIfAbsent(object, rootModel);
			}
		}

		// 3) Build declarations from LMObjects.
		final List<SymbolSpan> declarations = new ArrayList<>();
		final Map<LMObject, SymbolId> symbolIds = new IdentityHashMap<>();
		final Map<LMObject, SymbolSpan> spansByObject = new IdentityHashMap<>();

		for (final Map.Entry<LMObject, LinkNodeFull<?, PNode>> entry : nodeByObject.entrySet())
		{
			final LMObject object = entry.getKey();
			final SymbolId id = getOrCreateSymbolId(object, symbolIds, owningModel, rootModel);
			if (id == null)
			{
				continue;
			}

			final var span = resolveDeclarationSpan(object, entry.getValue(), source);
			final SymbolSpan symbolSpan = new SymbolSpan(id, span, null);

			spansByObject.put(object, symbolSpan);
		}

		// 3b) Enrich declarations with container symbols to establish a basic
		// hierarchy (for example Group -> Feature, Type -> Operation).
		for (final Map.Entry<LMObject, SymbolSpan> entry : spansByObject.entrySet())
		{
			final LMObject object = entry.getKey();
			final SymbolSpan baseSpan = entry.getValue();
			final SymbolId containerId = resolveContainerId(object, nodeByObject, symbolIds);
			declarations.add(new SymbolSpan(baseSpan.id(), baseSpan.span(), containerId));
		}

		// 4) Build references from resolved relations that target declared objects.
		final List<ReferenceSpan> references = new ArrayList<>();

		for (final LinkNode<?, PNode> root : linkTrees)
		{
			if (!(root instanceof LinkNodeFull<?, PNode> fullRoot))
			{
				continue;
			}

			fullRoot.streamTree().forEach(node -> {
				for (final ResolutionAttempt<Relation<?, ?>> attempt : node.relationResolutions())
				{
					final var resolution = attempt.resolution();
					final LMObject target;
					if (resolution instanceof RelationResolver.DynamicReferenceResolution<?> dyn)
					{
						try
						{
							target = dyn.linkNode().build();
						}
						catch (Exception e)
						{
							return;
						}
					}
					else if (resolution instanceof ModelReferenceResolver.StaticResolution staticResolution)
					{
						target = staticResolution.value();
					}
					else
					{
						// Other resolution types (for example constant literals) are not
						// represented as symbol references in this index.
						return;
					}

					final SymbolId targetId = getOrCreateSymbolId(target, symbolIds, owningModel, rootModel);
					if (targetId == null)
					{
						return;
					}

					final var refSpan = resolveReferenceSpan(attempt.feature(), node, source);
					references.add(new ReferenceSpan(targetId, refSpan));
				}
			});
		}

		// 5) Build header-based references for M1-style instance models: when a
		// document uses a meta-model as its "rootModel", each instance header
		// (for example 'CarParc' or 'ceo' in Peugeot.lm) implicitly refers to
		// either the owning meta-type (Group/Definition) or the containment
		// relation from the meta-model.
		//
		// We detect these by:
		// - Looking at each LinkNode's interpreted group() and containingRelation().
		// - Restricting to groups whose owning model is exactly rootModel (so we
		//   skip LMCore meta-groups when indexing meta-model definitions).
		// - Matching the header token against either the containment relation
		//   name (for example 'ceo') or the group name (for example 'CarParc').
		for (final LinkNode<?, PNode> root : linkTrees)
		{
			if (!(root instanceof LinkNodeFull<?, PNode> fullRoot))
			{
				continue;
			}

			fullRoot.streamTree().forEach(node -> {
				final var pNode = node.pNode();
				if (pNode == null)
				{
					return;
				}

				final var metaGroup = node.group();
				if (metaGroup == null)
				{
					return;
				}

				final Model owner = owningModel.get(metaGroup);
				if (!(owner instanceof MetaModel))
				{
					return;
				}

				final String headerToken = resolveHeaderToken(pNode);
				if (headerToken == null || headerToken.isBlank())
				{
					return;
				}

				// Decide whether this header denotes a relation (for example
				// 'ceo' in Peugeot.lm) or a type/alias (for example 'CarParc' or '-att').
				final Relation<?, ?> containment = node.containingRelation();
				LMObject target = null;

				if (containment != null && headerToken.equals(containment.name()))
				{
					// Header token matches the containment relation name:
					// treat it as a reference to the relation declaration in
					// the meta-model.
					target = containment;
				}
				else
				{
					// Fallback: treat the header as denoting the meta-type
					// itself, regardless of whether the raw token is a direct
					// group name ('Enum') or an alias ('-att', '+contains').
					target = metaGroup;
				}

				if (target == null)
				{
					return;
				}

				final SymbolId targetId = getOrCreateSymbolId(target, symbolIds, owningModel, owner);
				if (targetId == null)
				{
					return;
				}

				final var span = findValueSpan(headerToken, (LinkNodeFull<?, PNode>) node, source, false);
				if (span == null)
				{
					return;
				}

				references.add(new ReferenceSpan(targetId, span));
			});
		}

		// 6) Build references from model-level imports/metamodels attributes:
		// these are string-valued features on LMCore's Model/MetaModel types
		// that denote other models in the registry (for example
		// "test.multi.GraphCore" or "test.model.CarCompany").
		collectModelAttributeReferences(linkTrees,
										registry,
										owningModel,
										symbolIds,
										rootModel,
										references,
										source);

		return new LmSymbolIndex(List.copyOf(declarations), List.copyOf(references));
	}

	private static void collectModelAttributeReferences(final List<? extends LinkNode<?, PNode>> linkTrees,
													   final ModelRegistry registry,
													   final Map<LMObject, Model> owningModel,
													   final Map<LMObject, SymbolId> symbolIds,
													   final Model rootModel,
													   final List<ReferenceSpan> references,
													   final CharSequence source)
	{
		for (final LinkNode<?, PNode> root : linkTrees)
		{
			if (!(root instanceof LinkNodeFull<?, PNode> fullRoot))
			{
				continue;
			}

			fullRoot.streamTree().forEach(node -> {
				for (final ResolutionAttempt<Attribute<?, ?>> attempt : node.attributeResolutions())
				{
					final var feature = attempt.feature();
					if (feature == null || feature.values().isEmpty())
					{
						continue;
					}

					final var resolution = attempt.resolution();
					if (!(resolution instanceof AttributeResolver.AttributeResolution<?> attrResolution))
					{
						continue;
					}

					final var lmFeature = attrResolution.feature();
					final var featureName = lmFeature != null ? lmFeature.name() : null;
					if (!"imports".equals(featureName) &&
						!"metamodels".equals(featureName))
					{
						continue;
					}

					for (final String raw : feature.values())
					{
						if (raw == null)
						{
							continue;
						}
						final String name = raw.trim();
						if (name.isEmpty())
						{
							continue;
						}

						final Model targetModel = registry.getModel(name);
						if (targetModel == null)
						{
							continue;
						}

						if (!(targetModel instanceof LMObject targetObject))
						{
							continue;
						}

						final SymbolId targetId = getOrCreateSymbolId(targetObject, symbolIds, owningModel, rootModel);
						if (targetId == null)
						{
							continue;
						}

						final var span = findValueSpan(name, node, source, false);
						if (span == null)
						{
							continue;
						}

						references.add(new ReferenceSpan(targetId, span));
					}
				}
			});
		}
	}

	private static TextPositions.Span resolveReferenceSpan(final PFeature feature,
														   final LinkNodeFull<?, PNode> node,
														   final CharSequence source)
	{
		if (feature == null || feature.values().isEmpty())
		{
			return TextPositions.spanOf(node.pNode(), source);
		}

		final String raw = feature.values().getFirst();
		if (raw == null || raw.isBlank())
		{
			return TextPositions.spanOf(node.pNode(), source);
		}

		final var span = findValueSpan(raw, node, source, true);
		return span != null ? span : TextPositions.spanOf(node.pNode(), source);
	}

	private static TextPositions.Span resolveDeclarationSpan(final LMObject object,
															 final LinkNodeFull<?, PNode> node,
															 final CharSequence source)
	{
		final var resolved = resolveNameSpan(object, node, source);
		if (resolved == null)
		{
			return TextPositions.spanOf(node.pNode(), source);
		}
		return resolved.span();
	}

	private record NameSpan(String rawName, TextPositions.Span span)
	{
	}

	private static NameSpan resolveNameSpan(final LMObject object,
											final LinkNodeFull<?, PNode> node,
											final CharSequence source)
	{
		if (!(object instanceof org.logoce.lmf.model.lang.Named))
		{
			return null;
		}

		// Locate the 'name' attribute for this node so we can anchor
		// the declaration span on the actual name token (for example 'Person')
		// instead of the header keyword ('Definition').
		String rawName = null;

		for (final ResolutionAttempt<Attribute<?, ?>> attempt : node.attributeResolutions())
		{
			final var resolution = attempt.resolution();
			if (resolution instanceof AttributeResolver.AttributeResolution<?> attrResolution &&
				Named.Features.NAME == attrResolution.feature())
			{
				final var feature = attempt.feature();
				if (feature != null && !feature.values().isEmpty())
				{
					rawName = feature.values().getFirst();
				}
				else
				{
					rawName = attrResolution.value();
				}
				break;
			}
		}

		if (rawName == null || rawName.isBlank())
		{
			return null;
		}

		final var span = findValueSpan(rawName, node, source, false);
		if (span == null)
		{
			return null;
		}
		return new NameSpan(rawName, span);
	}

	private static TextPositions.Span findValueSpan(final String raw,
													final LinkNodeFull<?, PNode> node,
													final CharSequence source,
													final boolean expandPrefixes)
	{
		if (raw == null || raw.isBlank())
		{
			return null;
		}

		final var tokens = node.pNode().tokens();
		if (tokens.isEmpty())
		{
			return null;
		}

		final int startOffset = tokens.getFirst().offset();
		final var lastToken = tokens.getLast();
		final int endOffset = lastToken.offset() + Math.max(1, lastToken.length());
		if (startOffset < 0 || endOffset > source.length())
		{
			return null;
		}

		final String slice = source.subSequence(startOffset, endOffset).toString();
		final int idx = expandPrefixes ? slice.lastIndexOf(raw) : slice.indexOf(raw);
		if (idx < 0)
		{
			return null;
		}

		int valueOffset = startOffset + idx;
		int length;

		if (expandPrefixes)
		{
			int prefixOffset = valueOffset;
			while (prefixOffset > startOffset)
			{
				final char c = source.charAt(prefixOffset - 1);
				if (Character.isWhitespace(c) || c == '(' || c == ')' || c == '=')
				{
					break;
				}
				prefixOffset--;
			}

			valueOffset = prefixOffset;
			length = Math.max(1, (startOffset + idx + raw.length()) - valueOffset);
		}
		else
		{
			length = Math.max(1, raw.length());
		}

		final int line = TextPositions.lineFor(source, valueOffset);
		final int column = TextPositions.columnFor(source, valueOffset);

		return new TextPositions.Span(line, column, length, valueOffset);
	}

	private static SymbolId resolveContainerId(final LMObject object,
											   final Map<LMObject, LinkNodeFull<?, PNode>> nodeByObject,
											   final Map<LMObject, SymbolId> symbolIds)
	{
		final LinkNodeFull<?, PNode> node = nodeByObject.get(object);
		if (node == null)
		{
			return null;
		}

		final LinkNodeFull<?, PNode> parent = node.parent();
		if (parent == null)
		{
			return null;
		}

		try
		{
			final LMObject parentObject = parent.build();
			return symbolIds.get(parentObject);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private static String resolveContainerPath(final LMObject object)
	{
		// Prefer the semantic containment chain (lmContainer) so that the
		// container path is stable across documents and independent of the
		// current link tree. This allows external references (for example
		// '#GraphCore@Node') to compute the same SymbolId as the declaration
		// in the owning meta-model document.
		final var segments = new ArrayList<String>();
		LMObject cursor = object.lmContainer();

		while (cursor != null)
		{
			final String parentName = resolveName(cursor);
			if (parentName != null && !parentName.isBlank())
			{
				segments.add(parentName);
			}
			cursor = cursor.lmContainer();
		}

		if (segments.isEmpty())
		{
			return "";
		}

		java.util.Collections.reverse(segments);
		return String.join("/", segments);
	}

	private static SymbolKind classifySymbolKind(final LMObject object)
	{
		if (object instanceof MetaModel)
		{
			return SymbolKind.META_MODEL;
		}

		if (object instanceof org.logoce.lmf.model.lang.Group<?> ||
			object instanceof org.logoce.lmf.model.lang.Enum<?> ||
			object instanceof org.logoce.lmf.model.lang.Unit<?> ||
			object instanceof org.logoce.lmf.model.lang.JavaWrapper<?> ||
			object instanceof org.logoce.lmf.model.lang.Model)
		{
			return SymbolKind.TYPE;
		}

		if (object instanceof org.logoce.lmf.model.lang.Feature<?, ?> ||
			object instanceof org.logoce.lmf.model.lang.Generic ||
			object instanceof org.logoce.lmf.model.lang.Operation)
		{
			return SymbolKind.FEATURE;
		}

		// Other LMObjects (for example instance-level objects) are currently
		// not exposed as declarations; this can be extended in future revisions.
		return null;
	}

	private static String resolveName(final LMObject object)
	{
		if (!(object instanceof org.logoce.lmf.model.lang.Named named))
		{
			return null;
		}

		final Object value = named.name();
		return value != null ? value.toString() : null;
	}

	private static String[] resolveOwner(final Model model)
	{
		if (model instanceof MetaModel mm)
		{
			return new String[]{mm.domain(), mm.name()};
		}
		return new String[]{"", model.name()};
	}

	private static SymbolId getOrCreateSymbolId(final LMObject object,
												final Map<LMObject, SymbolId> symbolIds,
												final Map<LMObject, Model> owningModel,
												final Model defaultOwner)
	{
		final SymbolId existing = symbolIds.get(object);
		if (existing != null)
		{
			return existing;
		}

		final SymbolKind kind = classifySymbolKind(object);
		if (kind == null)
		{
			return null;
		}

		final String name = resolveName(object);
		if (name == null || name.isBlank())
		{
			return null;
		}

		final Model model = owningModel.getOrDefault(object, defaultOwner);
		if (model == null)
		{
			return null;
		}

		final String[] owner = resolveOwner(model);
		final String containerPath = resolveContainerPath(object);

		final SymbolId id = new SymbolId(owner[0], owner[1], kind, name, containerPath);
		symbolIds.put(object, id);
		return id;
	}

	private static String resolveHeaderToken(final PNode node)
	{
		final var tokens = node.tokens();
		if (tokens.isEmpty())
		{
			return null;
		}

		for (final var tok : tokens)
		{
			final String value = tok.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}
			return value;
		}

		return null;
	}
}
