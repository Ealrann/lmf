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
import org.logoce.lmf.lsp.features.DocumentSymbols;
import org.logoce.lmf.lsp.features.completion.LmCompletionEngine;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.LmSymbolKind;
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

		LOG.debug("LMF LSP didOpen: uri={}, version={}, textLength={}", uri, version,
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
			LOG.debug("LMF LSP didChange: uri={}, version={}, changes=0 (ignored)", uri, version);
			return;
		}

		final var lastChange = changes.getLast();
		final boolean hasRange = lastChange.getRange() != null;
		final int textLength = lastChange.getText() != null ? lastChange.getText().length() : 0;

		LOG.debug("LMF LSP didChange: uri={}, version={}, changeCount={}, lastChangeHasRange={}, lastChangeTextLength={}",
				  uri, version, changes.size(), hasRange, textLength);

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
				final var oldText = existing.text();
				final String effectiveText;

				if (hasRange && oldText != null)
				{
					final var range = lastChange.getRange();
					final int startOffset =
						org.logoce.lmf.model.util.TextPositions.offsetFor(
							oldText,
							range.getStart().getLine() + 1,
							range.getStart().getCharacter() + 1);
					final int endOffset =
						org.logoce.lmf.model.util.TextPositions.offsetFor(
							oldText,
							range.getEnd().getLine() + 1,
							range.getEnd().getCharacter() + 1);

					if (startOffset >= 0 && endOffset >= startOffset &&
						endOffset <= oldText.length())
					{
						final var sb = new StringBuilder();
						sb.append(oldText, 0, startOffset);
						sb.append(newText);
						if (endOffset < oldText.length())
						{
							sb.append(oldText, endOffset, oldText.length());
						}
						effectiveText = sb.toString();
					}
					else
					{
						// Fallback: if offsets cannot be computed, treat change text as full document.
						effectiveText = newText;
					}
				}
				else
				{
					effectiveText = newText;
				}

				existing.setText(effectiveText);
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

			// 1) Brace matching when caret is on a parenthesis.
			if (syntax != null)
			{
				final var parenHighlights = tryMatchParenthesis(syntax.source(), pos);
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

	private static List<DocumentHighlight> tryMatchParenthesis(final CharSequence source, final Position pos)
	{
		int offset = offsetForPosition(source, pos);
		if (offset < 0 || offset > source.length())
		{
			return List.of();
		}

		char ch = offset < source.length() ? source.charAt(offset) : '\0';
		if (ch != '(' && ch != ')')
		{
			// Caret is often just after the parenthesis; try the previous character on the same line.
			final int prev = offset - 1;
			if (prev >= 0)
			{
				final char prevCh = source.charAt(prev);
				if (prevCh == '(' || prevCh == ')')
				{
					offset = prev;
					ch = prevCh;
				}
			}
		}

		if (ch != '(' && ch != ')')
		{
			return List.of();
		}

		final int mateOffset;
		if (ch == '(')
		{
			mateOffset = findMatchingParenForward(source, offset);
		}
		else if (ch == ')')
		{
			mateOffset = findMatchingParenBackward(source, offset);
		}
		else
		{
			return List.of();
		}

		if (mateOffset < 0)
		{
			return List.of();
		}

		final var firstRange = rangeForOffset(source, offset);
		final var secondRange = rangeForOffset(source, mateOffset);

		final var first = new DocumentHighlight(firstRange, DocumentHighlightKind.Text);
		final var second = new DocumentHighlight(secondRange, DocumentHighlightKind.Text);
		return List.of(first, second);
	}

	private static int offsetForPosition(final CharSequence source, final Position pos)
	{
		final int line = pos.getLine() + 1;
		final int column = pos.getCharacter() + 1;
		return org.logoce.lmf.model.util.TextPositions.offsetFor(source, line, column);
	}

	private static int findMatchingParenForward(final CharSequence source, final int startOffset)
	{
		int depth = 0;
		for (int i = startOffset; i < source.length(); i++)
		{
			final char c = source.charAt(i);
			if (c == '(')
			{
				depth++;
			}
			else if (c == ')')
			{
				depth--;
				if (depth == 0)
				{
					return i;
				}
			}
		}
		return -1;
	}

	private static int findMatchingParenBackward(final CharSequence source, final int startOffset)
	{
		int depth = 0;
		for (int i = startOffset; i >= 0; i--)
		{
			final char c = source.charAt(i);
			if (c == ')')
			{
				depth++;
			}
			else if (c == '(')
			{
				depth--;
				if (depth == 0)
				{
					return i;
				}
			}
		}
		return -1;
	}

	private static org.eclipse.lsp4j.Range rangeForOffset(final CharSequence source, final int offset)
	{
		final int line = Math.max(0, org.logoce.lmf.model.util.TextPositions.lineFor(source, offset) - 1);
		final int col = Math.max(0, org.logoce.lmf.model.util.TextPositions.columnFor(source, offset) - 1);
		final var start = new Position(line, col);
		final var end = new Position(line, col + 1);
		return new org.eclipse.lsp4j.Range(start, end);
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

		return CompletableFuture.supplyAsync(() -> {
			final var state = server.workspaceIndex().getDocument(uri);
			if (state == null)
			{
				LOG.info("LMF LSP semanticTokensFull: uri={} no document state, tokens=0", uri);
				return new SemanticTokens(java.util.List.of());
			}

			var syntax = state.syntaxSnapshot();
			if (syntax == null)
			{
				LOG.info("LMF LSP semanticTokensFull: uri={} has no syntax snapshot yet, analyzing document", uri);
				server.analyzeDocument(state);
				syntax = state.syntaxSnapshot();
				if (syntax == null)
				{
					final var lastGood = state.lastGoodSyntaxSnapshot();
					if (lastGood != null)
					{
						LOG.info("LMF LSP semanticTokensFull: uri={} using lastGoodSyntaxSnapshot", uri);
						syntax = lastGood;
					}
				}

				if (syntax == null)
				{
					LOG.info("LMF LSP semanticTokensFull: uri={} no syntax available, tokens=0", uri);
					return new SemanticTokens(java.util.List.of());
				}
			}

			final java.util.List<Integer> data = new java.util.ArrayList<>();

			// Legend is ["keyword"], no modifiers.
			final int tokenTypeIndex = 0;
			final int modifiers = 0;

			final CharSequence source = syntax.source();

			// Collect the first non-whitespace token after '(' in each header node,
			// regardless of the specific keyword. This corresponds to the header
			// kind in LM headers, e.g. MetaModel, Group, Definition, Enum, etc.
			final var headerTokens = new java.util.ArrayList<int[]>();
			for (final var root : syntax.roots())
			{
				for (final var node : root.streamTree().toList())
				{
					final var pnode = node.data();
					final var tokens = pnode.tokens();
					if (tokens.isEmpty())
					{
						continue;
					}

					// Find first non-blank, non-"(" token.
					var headerToken = (org.logoce.lmf.model.resource.parsing.PToken) null;
					for (final var tok : tokens)
					{
						final String value = tok.value();
						if (value == null || value.isBlank() || "(".equals(value))
						{
							continue;
						}

						headerToken = tok;
						break;
					}

					if (headerToken == null)
					{
						continue;
					}

					final int offset = headerToken.offset();
					final int line = Math.max(0, org.logoce.lmf.model.util.TextPositions.lineFor(source, offset) - 1);
					final int character = Math.max(0, org.logoce.lmf.model.util.TextPositions.columnFor(source, offset) - 1);
					final int length = Math.max(1, headerToken.length());

					headerTokens.add(new int[]{line, character, length});
				}
			}

			if (headerTokens.isEmpty())
			{
				LOG.info("LMF LSP semanticTokensFull: uri={} no header tokens, tokens=0", uri);
				return new SemanticTokens(java.util.List.of());
			}

			headerTokens.sort((a, b) -> {
				final int cmpLine = Integer.compare(a[0], b[0]);
				if (cmpLine != 0) return cmpLine;
				return Integer.compare(a[1], b[1]);
			});

			int prevLine = 0;
			int prevChar = 0;

			for (final var token : headerTokens)
			{
				final int line = token[0];
				final int character = token[1];
				final int length = token[2];

				final int deltaLine = line - prevLine;
				final int deltaStart = deltaLine == 0 ? character - prevChar : character;

				data.add(deltaLine);
				data.add(deltaStart);
				data.add(length);
				data.add(tokenTypeIndex);
				data.add(modifiers);

				prevLine = line;
				prevChar = character;
			}

			final var tokens = new SemanticTokens(data);
			LOG.info("LMF LSP semanticTokensFull: uri={}, version={}, sourceLength={}, tokens={}",
					 uri, state.version(), source.length(), data.size() / 5);
			return tokens;
		}, server.worker());
	}

}
