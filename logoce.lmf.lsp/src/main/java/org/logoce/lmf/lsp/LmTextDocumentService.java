package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.logoce.lmf.lsp.features.DocumentSymbols;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.lang.Group;
import org.logoce.lmf.model.lang.JavaWrapper;
import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lang.Unit;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class LmTextDocumentService implements TextDocumentService
{
	private static final Logger LOG = LoggerFactory.getLogger(LmTextDocumentService.class);

	private enum CompletionContextKind
	{
		DEFAULT,
		LOCAL_AT,
		CROSS_MODEL_HASH
	}

	private final LmLanguageServer server;

	public LmTextDocumentService(final LmLanguageServer server)
	{
		this.server = server;
	}

	@Override
	public void didOpen(final DidOpenTextDocumentParams params)
	{
		final TextDocumentItem doc = params.getTextDocument();
		final URI uri = URI.create(doc.getUri());
		final int version = doc.getVersion();
		final String text = doc.getText();

		LOG.info("LMF LSP didOpen: uri={}, version={}, textLength={}", uri, version,
				 text != null ? text.length() : 0);

		server.worker().execute(() -> {
			final var state = new LmDocumentState(uri, version, text);
			server.workspaceIndex().putDocument(state);
			server.rebuildWorkspace();
		});
	}

	@Override
	public void didChange(final DidChangeTextDocumentParams params)
	{
		final TextDocumentIdentifier id = params.getTextDocument();
		final URI uri = URI.create(id.getUri());
		final int version = params.getTextDocument().getVersion();
		final List<TextDocumentContentChangeEvent> changes = params.getContentChanges();

		if (changes.isEmpty())
		{
			LOG.info("LMF LSP didChange: uri={}, version={}, changes=0 (ignored)", uri, version);
			return;
		}

		final var lastChange = changes.getLast();
		final boolean hasRange = lastChange.getRange() != null;
		final int textLength = lastChange.getText() != null ? lastChange.getText().length() : 0;

		LOG.info("LMF LSP didChange: uri={}, version={}, changeCount={}, lastChangeHasRange={}, lastChangeTextLength={}",
				 uri, version, changes.size(), hasRange, textLength);

		// For now we only support full document updates (no incremental ranges).
		final String newText = lastChange.getText();
		if (newText == null)
		{
			return;
		}

		server.worker().execute(() -> {
			final var index = server.workspaceIndex();
			final var existing = index.getDocument(uri);
			if (existing != null)
			{
				existing.setVersion(version);
				existing.setText(newText);
			}
			else
			{
				final var state = new LmDocumentState(uri, version, newText);
				index.putDocument(state);
			}
			server.rebuildWorkspace();
		});
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		server.worker().execute(() -> server.workspaceIndex().removeDocument(uri));
	}

	@Override
	public void didSave(final DidSaveTextDocumentParams params)
	{
		// No-op for now; future versions may trigger validation or workspace reindexing here.
	}

	@Override
	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
		final DocumentSymbolParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		return CompletableFuture.supplyAsync(() -> {
			final var state = server.workspaceIndex().getDocument(uri);
			if (state == null)
			{
				return List.<Either<SymbolInformation, DocumentSymbol>>of();
			}
			final SyntaxSnapshot syntax = state.syntaxSnapshot();
			if (syntax == null)
			{
				return List.<Either<SymbolInformation, DocumentSymbol>>of();
			}
			return DocumentSymbols.buildDocumentSymbols(syntax);
		}, server.worker());
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(final CompletionParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();
		return CompletableFuture.supplyAsync(() -> {
			final var state = server.workspaceIndex().getDocument(uri);
			if (state == null)
			{
				LOG.info("LMF LSP completion: no document state for uri={}", uri);
				return Either.forLeft(List.<CompletionItem>of());
			}

			final SyntaxSnapshot syntax = state.syntaxSnapshot();
			final SemanticSnapshot semantic = state.semanticSnapshot();
			if (syntax == null || semantic == null)
			{
				LOG.info("LMF LSP completion: missing snapshots for uri={}, syntaxNull={}, semanticNull={}",
						 uri, syntax == null, semantic == null);
				return Either.forLeft(List.<CompletionItem>of());
			}

			final CompletionContextKind contextKind = detectCompletionContext(syntax, pos);

			final Model model = semantic.model();
			if (!(model instanceof MetaModel mm))
			{
				LOG.info("LMF LSP completion: semantic model is not MetaModel for uri={}, modelClass={}",
						 uri, model != null ? model.getClass().getName() : "null");
				return Either.forLeft(List.<CompletionItem>of());
			}

			final var items = new ArrayList<CompletionItem>();
			final Set<String> seenLabels = new HashSet<>();

			for (final var item : basicCompletions())
			{
				if (seenLabels.add(item.getLabel()))
				{
					items.add(item);
				}
			}

			final ModelRegistry registry = server.workspaceIndex().modelRegistry();

			addMetaModelTypes(mm, null, false, items, seenLabels);

			for (final String imp : mm.imports())
			{
				final Model imported = registry.getModel(imp);
				if (imported instanceof MetaModel importedMm)
				{
					addMetaModelTypes(importedMm, importedMm.name(), true, items, seenLabels);
				}
			}

			final MetaModel lmCore = findLmCoreMetaModel(registry);
			if (lmCore != null)
			{
				addMetaModelTypes(lmCore, lmCore.name(), true, items, seenLabels);
			}
			final List<CompletionItem> shaped = shapeCompletionItems(items, contextKind);

			LOG.info("LMF LSP completion: uri={}, items={}, context={}", uri, shaped.size(), contextKind);

			return Either.forLeft(List.copyOf(shaped));
		}, server.worker());
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends org.eclipse.lsp4j.LocationLink>>> definition(
		final DefinitionParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();
		return CompletableFuture.supplyAsync(() -> {
			final var id = server.findTargetSymbol(uri, pos);
			if (id == null)
			{
				return Either.forLeft(List.<Location>of());
			}
			final var entry = server.workspaceIndex().symbolIndex().get(id);
			if (entry == null)
			{
				return Either.forLeft(List.<Location>of());
			}
			final var loc = new Location(entry.uri().toString(), entry.range());
			return Either.forLeft(List.of(loc));
		}, server.worker());
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(final ReferenceParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();
		return CompletableFuture.supplyAsync(() -> {
			final var id = server.findTargetSymbol(uri, pos);
			if (id == null)
			{
				return List.<Location>of();
			}
			final var refs = server.workspaceIndex().referenceIndex().getOrDefault(id, List.of());
			final var result = new ArrayList<Location>();
			final var decl = server.workspaceIndex().symbolIndex().get(id);
			if (decl != null && params.getContext().isIncludeDeclaration())
			{
				result.add(new Location(decl.uri().toString(), decl.range()));
			}
			for (final var ref : refs)
			{
				result.add(new Location(ref.uri().toString(), ref.range()));
			}
			return List.copyOf(result);
		}, server.worker());
	}

	@Override
	public CompletableFuture<Hover> hover(final HoverParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();
		return CompletableFuture.supplyAsync(() -> {
			final var id = server.findTargetSymbol(uri, pos);
			if (id == null)
			{
				return null;
			}
			final String kind = switch (id.kind())
			{
				case META_MODEL -> "MetaModel";
				case TYPE -> "Type";
				case FEATURE -> "Feature";
			};
			final String model = id.modelKey().qualifiedName();
			final String text = kind + " " + id.name() + " in " + model;
			final var markup = new MarkupContent("plaintext", text);
			return new Hover(markup);
		}, server.worker());
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(final RenameParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();
		final String newName = params.getNewName();

		return CompletableFuture.supplyAsync(() -> {
			if (server.settings().experimentalRename() == false)
			{
				return new WorkspaceEdit();
			}
			final var id = server.findTargetSymbol(uri, pos);
			if (id == null)
			{
				return new WorkspaceEdit();
			}
			// For now, only support TYPE and FEATURE renames; META_MODEL rename is deferred.
			if (id.kind() == org.logoce.lmf.lsp.state.LmSymbolKind.META_MODEL)
			{
				return new WorkspaceEdit();
			}

			final Map<String, List<TextEdit>> changes = new HashMap<>();

			final var decl = server.workspaceIndex().symbolIndex().get(id);
			if (decl != null)
			{
				addEdit(changes, decl.uri(), decl.range(), newName);
			}

			final var refs = server.workspaceIndex().referenceIndex().getOrDefault(id, List.of());
			for (final var ref : refs)
			{
				addEdit(changes, ref.uri(), ref.range(), newName);
			}

			final var edit = new WorkspaceEdit();
			final Map<String, List<TextEdit>> perUri = new HashMap<>();
			for (final var entry : changes.entrySet())
			{
				perUri.put(entry.getKey(), List.copyOf(entry.getValue()));
			}
			edit.setChanges(perUri);
			return edit;
		}, server.worker());
	}

	private static void addEdit(final Map<String, List<TextEdit>> changes,
								final URI uri,
								final org.eclipse.lsp4j.Range range,
								final String newText)
	{
		final var key = uri.toString();
		final var edits = changes.computeIfAbsent(key, k -> new ArrayList<>());
		edits.add(new TextEdit(range, newText));
	}

	private static List<CompletionItem> basicCompletions()
	{
		final List<CompletionItem> items = new ArrayList<>();
		for (final String label : List.of("MetaModel",
										  "Group",
										  "Definition",
										  "Enum",
										  "Unit",
										  "Alias",
										  "JavaWrapper",
										  "Generic",
										  "Operation",
										  "+att",
										  "-att",
										  "+contains",
										  "-contains",
										  "+refers",
										  "-refers"))
		{
			final var item = new CompletionItem(label);
			items.add(item);
		}
		return items;
	}

	private static List<CompletionItem> shapeCompletionItems(final List<CompletionItem> items,
															 final CompletionContextKind contextKind)
	{
		if (items.isEmpty())
		{
			return items;
		}

		final List<CompletionItem> result = new ArrayList<>();
		for (final var item : items)
		{
			final String label = item.getLabel();
			if (label == null || label.isEmpty())
			{
				continue;
			}

			final String detail = item.getDetail();
			final boolean isTypeItem = detail != null;

			switch (contextKind)
			{
				case LOCAL_AT ->
				{
					// In @ context, only suggest local type names (no cross-model, no keywords/aliases).
					if (label.startsWith("#") || !isTypeItem)
					{
						continue;
					}
					result.add(item);
				}
				case CROSS_MODEL_HASH ->
				{
					// In # context, only suggest cross-model type entries (#Model@Type).
					if (!label.startsWith("#") || !isTypeItem)
					{
						continue;
					}
					if (item.getInsertText() == null && label.length() > 1)
					{
						item.setInsertText(label.substring(1));
					}
					result.add(item);
				}
				default -> result.add(item);
			}
		}

		return result;
	}

	private static CompletionContextKind detectCompletionContext(final SyntaxSnapshot syntax, final Position pos)
	{
		final CharSequence source = syntax.source();
		for (final Tree<org.logoce.lmf.model.resource.parsing.PNode> root : syntax.roots())
		{
			final CompletionContextKind kind = detectInNode(root, source, pos);
			if (kind != null && kind != CompletionContextKind.DEFAULT)
			{
				return kind;
			}
		}
		return CompletionContextKind.DEFAULT;
	}

	private static CompletionContextKind detectInNode(final Tree<org.logoce.lmf.model.resource.parsing.PNode> node,
													 final CharSequence source,
													 final Position pos)
	{
		for (final PToken token : node.data().tokens())
		{
			final var range = rangeForToken(token, source);
			if (rangeContains(range, pos))
			{
				final String value = token.value();
				if (value == null || value.isEmpty())
				{
					return CompletionContextKind.DEFAULT;
				}
				final char first = value.charAt(0);
				if (first == '@')
				{
					return CompletionContextKind.LOCAL_AT;
				}
				if (first == '#')
				{
					return CompletionContextKind.CROSS_MODEL_HASH;
				}
				return CompletionContextKind.DEFAULT;
			}
		}

		for (final Tree<org.logoce.lmf.model.resource.parsing.PNode> child : node.children())
		{
			final CompletionContextKind kind = detectInNode(child, source, pos);
			if (kind != null && kind != CompletionContextKind.DEFAULT)
			{
				return kind;
			}
		}

		return CompletionContextKind.DEFAULT;
	}

	private static org.eclipse.lsp4j.Range rangeForToken(final PToken token, final CharSequence source)
	{
		final int start = token.offset();
		final int end = start + Math.max(1, token.length());
		final int startLine = Math.max(0, TextPositions.lineFor(source, start) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, start) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, end) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, end) - 1);
		final var startPos = new Position(startLine, startChar);
		final var endPos = new Position(endLine, endChar);
		return new org.eclipse.lsp4j.Range(startPos, endPos);
	}

	private static boolean rangeContains(final org.eclipse.lsp4j.Range range, final Position pos)
	{
		final Position start = range.getStart();
		final Position end = range.getEnd();

		if (pos.getLine() < start.getLine() || pos.getLine() > end.getLine())
		{
			return false;
		}
		if (pos.getLine() == start.getLine() && pos.getCharacter() < start.getCharacter())
		{
			return false;
		}
		if (pos.getLine() == end.getLine() && pos.getCharacter() > end.getCharacter())
		{
			return false;
		}
		return true;
	}

	private static void addMetaModelTypes(final MetaModel mm,
										  final String modelAlias,
										  final boolean useModelAliasInLabel,
										  final List<CompletionItem> items,
										  final Set<String> seenLabels)
	{
		final String modelQualifiedName = mm.domain() + "." + mm.name();
		final boolean crossModel = useModelAliasInLabel && modelAlias != null && !modelAlias.isEmpty();

		for (final Group<?> group : mm.groups())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, group.name(), "Group in " + modelQualifiedName);
		}
		for (final org.logoce.lmf.model.lang.Enum<?> _enum : mm.enums())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, _enum.name(), "Enum in " + modelQualifiedName);
		}
		for (final Unit<?> unit : mm.units())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, unit.name(), "Unit in " + modelQualifiedName);
		}
		for (final JavaWrapper<?> wrapper : mm.javaWrappers())
		{
			addTypeCompletion(items, seenLabels, crossModel, modelAlias, wrapper.name(), "JavaWrapper in " + modelQualifiedName);
		}
	}

	private static void addTypeCompletion(final List<CompletionItem> items,
										  final Set<String> seenLabels,
										  final boolean crossModel,
										  final String modelAlias,
										  final String typeName,
										  final String detail)
	{
		if (typeName == null || typeName.isEmpty())
		{
			return;
		}

		final String label;
		if (crossModel && modelAlias != null && !modelAlias.isEmpty())
		{
			label = "#" + modelAlias + "@" + typeName;
		}
		else
		{
			label = typeName;
		}

		if (seenLabels.add(label))
		{
			final var item = new CompletionItem(label);
			item.setDetail(detail);
			items.add(item);
		}
	}

	private static MetaModel findLmCoreMetaModel(final ModelRegistry registry)
	{
		for (final var model : (Iterable<Model>) registry.models()::iterator)
		{
			if (model instanceof MetaModel mm &&
				LMCorePackage.MODEL.domain().equals(mm.domain()) &&
				LMCorePackage.MODEL.name().equals(mm.name()))
			{
				return mm;
			}
		}
		return LMCorePackage.MODEL;
	}
}
