package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.logoce.lmf.lsp.features.DocumentSymbols;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class LmTextDocumentService implements TextDocumentService
{
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

		// For now we only support full document updates (no incremental ranges).
		final String newText = changes.isEmpty() ? null : changes.getLast().getText();
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
		return CompletableFuture.completedFuture(Either.forLeft(basicCompletions()));
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

}
