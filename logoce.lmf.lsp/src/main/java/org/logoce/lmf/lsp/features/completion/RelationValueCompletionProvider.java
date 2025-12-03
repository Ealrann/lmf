package org.logoce.lmf.lsp.features.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Concept;
import org.logoce.lmf.model.lang.Feature;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Relation;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Relation value completion.
 * <p>
 * This follows the same overall pattern as {@link AttributeValueCompletionProvider} but targets
 * relation-valued features: it uses the LMCore header and feature name to resolve the
 * {@link Relation} being edited and its {@link Concept} target, then walks the owning and
 * imported models to find objects whose {@code lmGroup()} is compatible with that concept.
 * Each matching object with a {@code name} feature is proposed as a reference:
 * <ul>
 *   <li>{@code @name} for objects in the owning model,</li>
 *   <li>{@code #ModelName@name} for objects in imported models (including LMCore).</li>
 * </ul>
 */
final class RelationValueCompletionProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(RelationValueCompletionProvider.class);

	private RelationValueCompletionProvider()
	{
	}

	static List<CompletionItem> complete(final CompletionContext context)
	{
		final SyntaxSnapshot syntax = context.syntax();
		final Position pos = context.position();

		if (syntax == null)
		{
			return List.of();
		}

		if (!SyntaxNavigation.hasEqualsBeforePosition(syntax, pos))
		{
			return List.of();
		}

		final SemanticSnapshot semantic = context.semantic();
		Relation<?, ?> relationFeature = null;

		// 1) Try to resolve via semantic link trees when available.
		if (semantic != null)
		{
			final var feature = SemanticNavigation.findFeatureAtValuePosition(semantic, syntax, pos);
			if (feature instanceof Relation<?, ?> r)
			{
				relationFeature = r;
			}
		}

		// 2) Fallback: resolve from header text (keyword + feature name) using LMCore,
		//    mirroring the attribute value completion strategy.
		if (relationFeature == null)
		{
			relationFeature = findRelationAtValuePosition(syntax, pos);
		}

		if (relationFeature == null)
		{
			return List.of();
		}

		final Concept<?> concept = relationFeature.concept();
		if (concept == null)
		{
			LOG.info("LMF LSP completion: relation value – resolved relation '{}' has null concept at line={}, character={}",
					 relationFeature.name(), pos.getLine(), pos.getCharacter());
			return List.of();
		}

		final Model owningModel = semantic != null ? semantic.model() : null;
		final ModelRegistry registry = context.server().workspaceIndex().modelRegistry();
		final List<CompletionItem> items = buildRelationValueCompletions(owningModel, concept, registry, pos);
		if (!items.isEmpty())
		{
			LOG.info("LMF LSP completion: relation value completions, feature={}, items={}",
					 relationFeature.name(), items.size());
		}
		return items;
	}

	private static List<CompletionItem> buildRelationValueCompletions(final Model owningModel,
																	  final Concept<?> concept,
																	  final ModelRegistry registry,
																	  final Position pos)
	{
		final var locals = new ArrayList<CompletionItem>();
		final var cross = new ArrayList<CompletionItem>();

		if (owningModel != null)
		{
			collectCandidatesForModel(owningModel, owningModel, concept, true, locals, cross, pos);

			for (final String imp : owningModel.imports())
			{
				final Model imported = registry.getModel(imp);
				if (imported == null)
				{
					continue;
				}
				collectCandidatesForModel(imported, owningModel, concept, false, locals, cross, pos);
			}
		}

		// LMCore is implicitly available and always considered as a cross-model source
		// for concept-compatible objects.
		collectCandidatesForModel(LMCorePackage.MODEL, owningModel, concept, false, locals, cross, pos);

		if (locals.isEmpty() && cross.isEmpty())
		{
			return List.of();
		}

		final var all = new ArrayList<CompletionItem>(locals.size() + cross.size());
		all.addAll(locals);
		all.addAll(cross);

		LOG.debug("Relation completion: owningModel={} conceptGroup={} local={} cross={}",
				  qualifiedName(owningModel), concept.lmGroup().name(), locals.size(), cross.size());

		return List.copyOf(all);
	}

	private static Relation<?, ?> findRelationAtValuePosition(final SyntaxSnapshot syntax,
															  final Position pos)
	{
		final CharSequence source = syntax.source();
		final int targetLine = pos.getLine();
		final int targetChar = pos.getCharacter();

		int line = 0;
		int lineStartOffset = -1;
		for (int i = 0; i < source.length(); i++)
		{
			final char c = source.charAt(i);
			if (line == targetLine)
			{
				lineStartOffset = i;
				break;
			}
			if (c == '\n')
			{
				line++;
			}
		}

		if (lineStartOffset == -1)
		{
			return null;
		}

		int lineEndOffset = source.length();
		for (int i = lineStartOffset; i < source.length(); i++)
		{
			if (source.charAt(i) == '\n')
			{
				lineEndOffset = i;
				break;
			}
		}

		final String lineText = source.subSequence(lineStartOffset, lineEndOffset).toString();

		final int openIdx = lineText.indexOf('(');
		if (openIdx < 0)
		{
			return null;
		}

		int closeIdx = lineText.indexOf(')', openIdx + 1);
		if (closeIdx < 0)
		{
			closeIdx = lineText.length();
		}

		final int headerStart = openIdx + 1;
		final int headerEnd = closeIdx;
		if (headerStart >= headerEnd)
		{
			return null;
		}

		final String header = lineText.substring(headerStart, headerEnd);
		final int headerCaret = Math.min(Math.max(0, targetChar - headerStart), header.length());

		final var tokens = AttributeValueCompletionProvider.tokenizeHeader(header);
		if (tokens.isEmpty())
		{
			return null;
		}

		final String keyword = tokens.getFirst().text();
		if (keyword == null || keyword.isBlank())
		{
			return null;
		}

		final String featureName = AttributeValueCompletionProvider.resolveFeatureNameAtCaret(tokens, headerCaret);
		if (featureName == null || featureName.isBlank())
		{
			return null;
		}

		final String groupName = AttributeValueCompletionProvider.resolveGroupNameForHeaderKeyword(keyword);
		if (groupName == null || groupName.isBlank())
		{
			return null;
		}

		final MetaModel lmCore = LMCorePackage.MODEL;
		Group<?> group = null;
		for (final Group<?> g : lmCore.groups())
		{
			if (groupName.equals(g.name()))
			{
				group = g;
				break;
			}
		}

		if (group == null)
		{
			return null;
		}

		for (final Feature<?, ?> feature : group.features())
		{
			if (feature instanceof Relation<?, ?> r && featureName.equals(feature.name()))
			{
				return r;
			}
		}

		return null;
	}

	private static void collectCandidatesForModel(final Model model,
												  final Model owningModel,
												  final Concept<?> concept,
												  final boolean local,
												  final List<CompletionItem> locals,
												  final List<CompletionItem> cross,
												  final Position pos)
	{
		if (!(model instanceof LMObject root))
		{
			return;
		}

		final String qualified = qualifiedName(model);
		final String alias = model.name();

		for (final LMObject object : (Iterable<LMObject>) ModelUtils.streamTree(root)::iterator)
		{
			final Group<?> group = object.lmGroup();
			if (!ModelUtils.isSubGroup(concept, group))
			{
				continue;
			}

			final Feature<?, ?> nameFeature = findNameFeature(group);
			if (nameFeature == null)
			{
				// TODO: handle unnamed objects later.
				continue;
			}

			final Object value = object.get(nameFeature);
			if (value == null)
			{
				continue;
			}
			final String name = value.toString();
			if (name.isBlank())
			{
				continue;
			}

			final String label;
			if (local && Objects.equals(model, owningModel))
			{
				label = "@" + name;
			}
			else
			{
				label = "#" + alias + "@" + name;
			}

			final var item = new CompletionItem(label);
			item.setDetail(group.name() + " in " + qualified);

			final var range = new Range(new Position(pos.getLine(), pos.getCharacter()),
										new Position(pos.getLine(), pos.getCharacter()));
			item.setTextEdit(Either.forLeft(new TextEdit(range, label)));

			if (local && Objects.equals(model, owningModel))
			{
				locals.add(item);
			}
			else
			{
				cross.add(item);
			}
		}
	}

	private static Feature<?, ?> findNameFeature(final Group<?> group)
	{
		for (final Feature<?, ?> feature : group.features())
		{
			if ("name".equals(feature.name()))
			{
				return feature;
			}
		}
		return null;
	}

	private static String qualifiedName(final Model model)
	{
		if (model == null)
		{
			return "<unknown>";
		}

		if (model instanceof MetaModel mm)
		{
			final String domain = Objects.toString(mm.domain(), "");
			final String name = Objects.toString(mm.name(), "");
			return domain.isBlank() ? name : domain + "." + name;
		}

		return Objects.toString(model.name(), "");
	}
}
