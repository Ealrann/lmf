package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.logoce.lmf.lsp.features.DocumentSymbols;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.LmSymbolKind;
import org.logoce.lmf.lsp.state.ModelKey;
import org.logoce.lmf.lsp.state.ReferenceOccurrence;
import org.logoce.lmf.lsp.state.SymbolEntry;
import org.logoce.lmf.lsp.state.SymbolId;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.loader.linking.feature.reference.PathParser;
import org.logoce.lmf.model.loader.linking.feature.reference.PathUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public final class SymbolIndexer
{
	private static final Logger LOG = LoggerFactory.getLogger(SymbolIndexer.class);

	private final WorkspaceIndex workspaceIndex;

	public SymbolIndexer(final WorkspaceIndex workspaceIndex)
	{
		this.workspaceIndex = workspaceIndex;
	}

	public void rebuildIndicesForDocument(final LmDocumentState state)
	{
		final var syntax = state.syntaxSnapshot();
		final var effectiveSemantic = state.lastGoodSemanticSnapshot() != null
									  ? state.lastGoodSemanticSnapshot()
									  : state.semanticSnapshot();
		final var semantic = effectiveSemantic;
		if (syntax == null || semantic == null)
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final var model = semantic.model();
		if (!(model instanceof MetaModel mm))
		{
			workspaceIndex.clearIndicesForDocument(state.uri());
			return;
		}

		final var modelKey = new ModelKey(mm.domain(), mm.name());

		final var documentSymbols = DocumentSymbols.buildDocumentSymbols(syntax);
		final var symbolEntries = new ArrayList<SymbolEntry>();

		for (final var either : documentSymbols)
		{
			if (either.isRight())
			{
				collectSymbolEntries(modelKey, either.getRight(), symbolEntries, state.uri());
			}
		}

		workspaceIndex.registerSymbols(state.uri(), symbolEntries);

		final var references = buildReferences(modelKey, mm, syntax, state.uri());
		workspaceIndex.registerReferences(state.uri(), references);

		LOG.debug("LMF LSP SymbolIndexer: uri={}, symbols={}, references={}",
				  state.uri(), symbolEntries.size(), references.size());
	}

	private static void collectSymbolEntries(final ModelKey modelKey,
											 final DocumentSymbol symbol,
											 final List<SymbolEntry> out,
											 final URI uri)
	{
		final var kind = toSymbolKind(symbol);
		if (kind != null)
		{
			final var id = new SymbolId(modelKey, kind, symbol.getName());
			out.add(new SymbolEntry(id, uri, symbol.getRange()));
		}

		final var children = symbol.getChildren();
		if (children != null)
		{
			for (final var child : children)
			{
				collectSymbolEntries(modelKey, child, out, uri);
			}
		}
	}

	private List<ReferenceOccurrence> buildReferences(final ModelKey modelKey,
													  final MetaModel model,
													  final SyntaxSnapshot syntax,
													  final URI uri)
	{
		final var references = new ArrayList<ReferenceOccurrence>();
		final CharSequence source = syntax.source();
		final ModelRegistry registry = workspaceIndex.modelRegistry();

		for (final Tree<PNode> root : syntax.roots())
		{
			collectReferencesInNode(root, modelKey, model, source, uri, registry, references);
		}

		return List.copyOf(references);
	}

	private void collectReferencesInNode(final Tree<PNode> node,
										 final ModelKey modelKey,
										 final MetaModel currentModel,
										 final CharSequence source,
										 final URI uri,
										 final ModelRegistry registry,
										 final List<ReferenceOccurrence> out)
	{
		final var tokens = node.data().tokens();
		for (final PToken token : tokens)
		{
			final String value = token.value();
			if (value == null || value.isEmpty())
			{
				continue;
			}
			final char first = value.charAt(0);
			if (first == '@')
			{
				final String typeName = value.substring(1);
				if (!typeName.isEmpty())
				{
					final var id = new SymbolId(modelKey, LmSymbolKind.TYPE, typeName);
					if (workspaceIndex.symbolIndex().containsKey(id))
					{
						final Range range = rangeForToken(token, source);
						out.add(new ReferenceOccurrence(id, uri, range));
					}
				}
			}
			else if (first == '#')
			{
				final var parsed = PathUtil.parse(value);
				String modelName = null;
				String targetType = null;

				for (final var segment : parsed.segments())
				{
					if (segment.type() == PathParser.Type.MODEL && modelName == null)
					{
						modelName = segment.text();
					}
					else if (segment.type() == PathParser.Type.NAME)
					{
						targetType = segment.text();
					}
				}

				if (modelName != null && targetType != null)
				{
					ModelKey targetKey = resolveModelAlias(currentModel, modelName, registry);
					if (targetKey == null)
					{
						targetKey = new ModelKey("", modelName);
					}

					final var id = new SymbolId(targetKey, LmSymbolKind.TYPE, targetType);
					if (workspaceIndex.symbolIndex().containsKey(id))
					{
						final Range range = rangeForToken(token, source);
						out.add(new ReferenceOccurrence(id, uri, range));
					}
				}
			}
		}

		for (final Tree<PNode> child : node.children())
		{
			collectReferencesInNode(child, modelKey, currentModel, source, uri, registry, out);
		}
	}

	private static ModelKey resolveModelAlias(final MetaModel currentModel,
											  final String alias,
											  final ModelRegistry registry)
	{
		if (alias == null || alias.isEmpty())
		{
			return null;
		}

		// LMCore is implicitly available for all M2 models.
		if (alias.equals(LMCorePackage.MODEL.name()))
		{
			return new ModelKey(LMCorePackage.MODEL.domain(), LMCorePackage.MODEL.name());
		}

		// First, resolve via imports on the current meta-model.
		for (final String imp : currentModel.imports())
		{
			final int lastDot = imp.lastIndexOf('.');
			final String simpleName = lastDot >= 0 ? imp.substring(lastDot + 1) : imp;
			if (simpleName.equals(alias))
			{
				final Model imported = registry.getModel(imp);
				if (imported instanceof MetaModel mm)
				{
					return new ModelKey(mm.domain(), mm.name());
				}
			}
		}

		// Fallback: scan the registry for the first meta-model with matching simple name.
		for (final Model model : (Iterable<Model>) registry.models()::iterator)
		{
			if (model instanceof MetaModel mm && mm.name().equals(alias))
			{
				return new ModelKey(mm.domain(), mm.name());
			}
		}

		return null;
	}

	private static Range rangeForToken(final PToken token, final CharSequence source)
	{
		final int start = token.offset();
		final int end = start + Math.max(1, token.length());
		final int startLine = Math.max(0, TextPositions.lineFor(source, start) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, start) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, end) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, end) - 1);
		final var startPos = new Position(startLine, startChar);
		final var endPos = new Position(endLine, endChar);
		return new Range(startPos, endPos);
	}

	private static LmSymbolKind toSymbolKind(final DocumentSymbol symbol)
	{
		return switch (symbol.getKind())
		{
			case Namespace -> LmSymbolKind.META_MODEL;
			case Class, Enum, Struct -> LmSymbolKind.TYPE;
			case Field, Method, TypeParameter, Constant -> LmSymbolKind.FEATURE;
			default -> null;
		};
	}
}
