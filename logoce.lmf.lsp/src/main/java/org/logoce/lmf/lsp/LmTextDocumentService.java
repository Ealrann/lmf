package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;
import org.logoce.lmf.lsp.features.DocumentSymbols;
import org.logoce.lmf.lsp.features.completion.LmCompletionEngine;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class LmTextDocumentService implements TextDocumentService
{
	private static final Logger LOG = LoggerFactory.getLogger(LmTextDocumentService.class);

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
		final String effectiveText = text != null ? text : "";

		LOG.info("LMF LSP didOpen: uri={}, version={}, textLength={}", uri, version,
				 effectiveText.length());

		server.worker().execute(() -> {
			server.openDocument(uri, version, effectiveText);
			server.refreshSemanticTokensIfSupported("didOpen");
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
			LOG.debug("LMF LSP didChange: uri={}, version={}, changes=0 (ignored)", uri, version);
			return;
		}

		if (changes.size() > 1)
		{
			LOG.info("LMF LSP didChange: uri={}, version={}, changeCount={} (applying sequentially)",
					 uri, version, changes.size());
		}

		server.worker().execute(() -> {
			final var existing = server.workspaceIndex().getDocument(uri);
			String updatedText = existing != null && existing.text() != null ? existing.text() : "";
			for (final var change : changes)
			{
				updatedText = applyChange(uri, updatedText, change);
			}

			server.updateDocument(uri, version, updatedText);
		});
	}

	private static String applyChange(final URI uri,
									  final String currentText,
									  final TextDocumentContentChangeEvent change)
	{
		final String newText = change.getText();
		if (newText == null)
		{
			return currentText;
		}

		final var range = change.getRange();
		if (range == null)
		{
			return newText;
		}

		final int startOffset =
			TextPositions.offsetFor(
				currentText,
				range.getStart().getLine() + 1,
				range.getStart().getCharacter() + 1);
		final int endOffset =
			TextPositions.offsetFor(
				currentText,
				range.getEnd().getLine() + 1,
				range.getEnd().getCharacter() + 1);

		if (startOffset < 0 || endOffset < startOffset || endOffset > currentText.length())
		{
			LOG.warn("LMF LSP didChange: uri={} invalid range ({}:{})-({}:{}) for textLength={}, applying as full document",
					 uri,
					 range.getStart().getLine(), range.getStart().getCharacter(),
					 range.getEnd().getLine(), range.getEnd().getCharacter(),
					 currentText.length());
			return newText;
		}

		final var sb = new StringBuilder();
		sb.append(currentText, 0, startOffset);
		sb.append(newText);
		if (endOffset < currentText.length())
		{
			sb.append(currentText, endOffset, currentText.length());
		}
		return sb.toString();
	}

	@Override
	public void didClose(final DidCloseTextDocumentParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		LOG.info("LMF LSP didClose: uri={}", uri);
		server.worker().execute(() -> {
			server.closeDocument(uri);
			server.refreshSemanticTokensIfSupported("didClose");
		});
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
			return DocumentSymbols.buildDocumentSymbols(syntax, state.semanticSnapshot(), server.workspaceIndex(), uri);
		}, server.worker());
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(final DocumentHighlightParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();

		return CompletableFuture.supplyAsync(() -> {
			final var state = server.workspaceIndex().getDocument(uri);
			final var syntax = state != null ? state.syntaxSnapshot() : null;

			if (syntax != null)
			{
				final var parenHighlights = BraceMatcher.matchParenthesis(syntax.source(), pos);
				if (!parenHighlights.isEmpty())
				{
					return parenHighlights;
				}
			}

			// 2) Fallback: symbol-based highlights (any resolved symbol).
			final var id = server.findTargetSymbol(uri, pos);
			if (id == null)
			{
				return List.<DocumentHighlight>of();
			}

			final var index = server.workspaceIndex();
			final var result = new ArrayList<DocumentHighlight>();

			// Declaration in this file (if any).
			final var decl = index.symbolIndex().get(id);
			if (decl != null && uri.equals(decl.uri()))
			{
				final var dh = new DocumentHighlight(decl.range(), DocumentHighlightKind.Text);
				result.add(dh);
			}

			// References in this file.
			final var refs = index.referenceIndex().getOrDefault(id, List.of());
			for (final var ref : refs)
			{
				if (uri.equals(ref.uri()))
				{
					final var dh = new DocumentHighlight(ref.range(), DocumentHighlightKind.Text);
					result.add(dh);
				}
			}

			return List.copyOf(result);
		}, server.worker());
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(final CompletionParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());
		final Position pos = params.getPosition();
		return CompletableFuture.supplyAsync(() -> {
			final var result = LmCompletionEngine.complete(server, uri, pos);
			if (result.isLeft())
			{
				final List<CompletionItem> items = result.getLeft();
				LOG.debug("LMF LSP completion: server returned {} items at uri={}, line={}, character={}",
						  items.size(), uri, pos.getLine(), pos.getCharacter());
			}
			else
			{
				final CompletionList list = result.getRight();
				final List<CompletionItem> items = list.getItems();
				LOG.debug("LMF LSP completion: server returned CompletionList with {} items at uri={}, line={}, character={}",
						  items.size(), uri, pos.getLine(), pos.getCharacter());
			}
			return result;
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

	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(final SemanticTokensParams params)
	{
		final URI uri = URI.create(params.getTextDocument().getUri());

		return CompletableFuture.supplyAsync(() -> SemanticTokensProvider.computeSemanticTokens(server, uri),
											 server.worker());
	}

}
