package org.logoce.lmf.lsp.features;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.LspRanges;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.lsp.state.SymbolEntry;
import org.logoce.lmf.lsp.state.SymbolId;
import org.logoce.lmf.lsp.state.WorkspaceIndex;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.loader.api.text.syntax.PToken;
import org.logoce.lmf.core.util.tree.Tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DocumentSymbols
{
	private DocumentSymbols()
	{
	}

	public static List<Either<SymbolInformation, DocumentSymbol>> buildDocumentSymbols(final SyntaxSnapshot syntax,
																					   final SemanticSnapshot semantic,
																					   final WorkspaceIndex workspaceIndex,
																					   final java.net.URI uri)
	{
		if (syntax == null)
		{
			return List.of();
		}

		if (semantic == null || workspaceIndex == null || uri == null)
		{
			return buildDocumentSymbols(syntax);
		}

		final List<SymbolEntry> entries = workspaceIndex.symbolsForUri(uri);
		if (entries.isEmpty())
		{
			return buildDocumentSymbols(syntax);
		}

		final Map<SymbolId, DocumentSymbol> byId = new LinkedHashMap<>();

		for (final SymbolEntry entry : entries)
		{
			final SymbolId id = entry.id();
			final SymbolKind kind = switch (id.kind())
			{
				case META_MODEL -> SymbolKind.Namespace;
				case TYPE -> SymbolKind.Class;
				case FEATURE -> SymbolKind.Field;
			};

			final var symbol = new DocumentSymbol(id.name(), kind, entry.range(), entry.range());
			byId.put(id, symbol);
		}

		final var topLevel = new ArrayList<DocumentSymbol>();
		final var attachedAsChild = new java.util.HashSet<SymbolId>();

		for (final SymbolEntry entry : entries)
		{
			final var containerId = entry.container();
			if (containerId == null)
			{
				continue;
			}

			final var symbol = byId.get(entry.id());
			final var container = byId.get(containerId);
			if (symbol == null || container == null)
			{
				continue;
			}

			addChild(container, symbol);
			attachedAsChild.add(entry.id());
		}

		for (final SymbolEntry entry : entries)
		{
			if (attachedAsChild.contains(entry.id()))
			{
				continue;
			}
			final var symbol = byId.get(entry.id());
			if (symbol != null)
			{
				topLevel.add(symbol);
			}
		}

		return topLevel.stream()
					   .map(ds -> Either.<SymbolInformation, DocumentSymbol>forRight(ds))
					   .toList();
	}

	private static void addChild(final DocumentSymbol container, final DocumentSymbol child)
	{
		List<DocumentSymbol> children = container.getChildren();
		if (children == null)
		{
			children = new ArrayList<>();
		}
		children.add(child);
		container.setChildren(children);
	}

	public static List<Either<SymbolInformation, DocumentSymbol>> buildDocumentSymbols(final SyntaxSnapshot snapshot)
	{
		if (snapshot == null)
		{
			return List.of();
		}

		final List<DocumentSymbol> topLevel = new ArrayList<>();
		final CharSequence source = snapshot.source();

		for (final Tree<PNode> root : snapshot.roots())
		{
			final PNode node = root.data();
			final var tokens = node.tokens();
			if (tokens.isEmpty())
			{
				continue;
			}

			final PToken headToken = tokens.getFirst();
			final String head = headToken.value();
			if (head == null || head.isBlank() || "(".equals(head))
			{
				continue;
			}

			final Range range = rangeForNode(node, source);
			final Range selectionRange = rangeForToken(headToken, source);
			final var symbol = new DocumentSymbol(head, SymbolKind.Class, range, selectionRange);
			topLevel.add(symbol);
		}

		return topLevel.stream()
					   .map(ds -> Either.<SymbolInformation, DocumentSymbol>forRight(ds))
					   .toList();
	}

	private static Range rangeForNode(final PNode node, final CharSequence source)
	{
		final var tokens = node.tokens();
		if (tokens.isEmpty())
		{
			final var pos = new Position(0, 0);
			return new Range(pos, pos);
		}

		int start = Integer.MAX_VALUE;
		int end = Integer.MIN_VALUE;
		for (final var tok : tokens)
		{
			start = Math.min(start, tok.offset());
			end = Math.max(end, tok.offset() + tok.length());
		}
		return LspRanges.forOffsets(source, start, end);
	}

	private static Range rangeForToken(final PToken token, final CharSequence source)
	{
		return LspRanges.forToken(source, token);
	}
}
