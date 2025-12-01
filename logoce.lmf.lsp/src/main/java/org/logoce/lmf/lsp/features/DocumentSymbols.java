package org.logoce.lmf.lsp.features;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.logoce.lmf.lsp.state.SemanticSnapshot;
import org.logoce.lmf.lsp.state.SyntaxSnapshot;
import org.logoce.lmf.model.loader.model.LmSymbolIndex;
import org.logoce.lmf.model.loader.model.LmSymbolIndexBuilder;
import org.logoce.lmf.model.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PToken;
import org.logoce.lmf.model.util.TextPositions;
import org.logoce.lmf.model.util.tree.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DocumentSymbols
{
	private DocumentSymbols()
	{
	}

	public static List<Either<SymbolInformation, DocumentSymbol>> buildDocumentSymbols(final SyntaxSnapshot syntax,
																					   final SemanticSnapshot semantic)
	{
		if (semantic == null || semantic.model() == null)
		{
			return buildDocumentSymbols(syntax);
		}

		final var index = LmSymbolIndexBuilder.buildIndex(
			semantic.model(),
			syntax.roots(),
			syntax.source(),
			org.logoce.lmf.model.util.ModelRegistry.empty());

		final var byId = new java.util.LinkedHashMap<LmSymbolIndex.SymbolId, DocumentSymbol>();

		for (final LmSymbolIndex.SymbolSpan decl : index.declarations())
		{
			final var id = decl.id();
			final SymbolKind kind = switch (id.kind())
			{
				case META_MODEL -> SymbolKind.Namespace;
				case TYPE -> SymbolKind.Class;
				case FEATURE -> SymbolKind.Field;
			};

			final var range = spanToRange(decl.span(), syntax.source());
			final var symbol = new DocumentSymbol(id.name(), kind, range, range);
			byId.put(id, symbol);
		}

		// Attach children to their containers using the container id from the index.
		for (final LmSymbolIndex.SymbolSpan decl : index.declarations())
		{
			final var containerId = decl.container();
			if (containerId == null)
			{
				continue;
			}
			final var child = byId.get(decl.id());
			final var container = byId.get(containerId);
			if (child == null || container == null)
			{
				continue;
			}
			addChild(container, child);
		}

		// Top-level symbols are those without a container.
		final var topLevel = new ArrayList<DocumentSymbol>();
		for (final LmSymbolIndex.SymbolSpan decl : index.declarations())
		{
			if (decl.container() == null)
			{
				final var symbol = byId.get(decl.id());
				if (symbol != null)
				{
					topLevel.add(symbol);
				}
			}
		}

		return topLevel.stream()
					  .map(ds -> Either.<SymbolInformation, DocumentSymbol>forRight(ds))
					  .toList();
	}

	public static List<Either<SymbolInformation, DocumentSymbol>> buildDocumentSymbols(final SyntaxSnapshot snapshot)
	{
		final List<DocumentSymbol> topLevel = new ArrayList<>();
		for (final Tree<PNode> root : snapshot.roots())
		{
			collectSymbols(root, null, topLevel, snapshot.source());
		}
		return topLevel.stream()
					   .map(ds -> Either.<SymbolInformation, DocumentSymbol>forRight(ds))
					   .toList();
	}

	private static void collectSymbols(final Tree<PNode> node,
									   final DocumentSymbol currentContainer,
									   final List<DocumentSymbol> topLevel,
									   final CharSequence source)
	{
		final var pnode = node.data();
		final var tokens = pnode.tokens();
		final String head = tokens.isEmpty() ? "" : tokens.getFirst().value();

		final SymbolKind kind = mapKind(head);
		DocumentSymbol nextContainer = currentContainer;

		if (isContainerKind(kind))
		{
			final var nameToken = resolveNameToken(tokens);
			String name = nameToken != null ? nameToken.value() : head;

			// For model-level MetaModel headers, prefer the semantic model name
			// parsed via ModelHeaderUtil over the raw token heuristics.
			if (kind == SymbolKind.Namespace && "MetaModel".equals(head))
			{
				try
				{
					final String modelName = ModelHeaderUtil.resolveName(pnode);
					if (modelName != null && !modelName.isEmpty())
					{
						name = modelName;
					}
				}
				catch (IllegalStateException ignored)
				{
					// Fall back to the token-derived name.
				}
			}
			final Range range = rangeForNode(pnode, source);
			final Range selectionRange = nameToken != null ? rangeForToken(nameToken, source) : range;

			final var container = new DocumentSymbol(Objects.requireNonNullElse(name, head), kind, range, selectionRange);

			if (currentContainer != null)
			{
				addChild(currentContainer, container);
			}
			else
			{
				topLevel.add(container);
			}
			nextContainer = container;
		}
		else if (isLeafKind(kind) && currentContainer != null)
		{
			final var nameToken = resolveNameToken(tokens);
			final String name = nameToken != null ? nameToken.value() : head;
			final Range range = rangeForNode(pnode, source);
			final Range selectionRange = nameToken != null ? rangeForToken(nameToken, source) : range;

			final var symbol = new DocumentSymbol(Objects.requireNonNullElse(name, head), kind, range, selectionRange);
			addChild(currentContainer, symbol);
		}

		for (final Tree<PNode> child : node.children())
		{
			collectSymbols(child, nextContainer, topLevel, source);
		}
	}

	private static boolean isContainerKind(final SymbolKind kind)
	{
		return kind == SymbolKind.Class ||
			   kind == SymbolKind.Enum ||
			   kind == SymbolKind.Struct ||
			   kind == SymbolKind.Namespace;
	}

	private static boolean isLeafKind(final SymbolKind kind)
	{
		return kind == SymbolKind.Field ||
			   kind == SymbolKind.Method ||
			   kind == SymbolKind.TypeParameter ||
			   kind == SymbolKind.Constant;
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

	private static SymbolKind mapKind(final String head)
	{
		if (head == null) return null;
		final String trimmed = head.trim();
		return switch (trimmed)
		{
			case "MetaModel" -> SymbolKind.Namespace;
			case "Group" -> SymbolKind.Class;
			case "Definition" -> SymbolKind.Class;
			case "Enum" -> SymbolKind.Enum;
			case "Unit" -> SymbolKind.Struct;
			case "JavaWrapper" -> SymbolKind.Class;
			case "Alias" -> SymbolKind.Constant;
			case "Generic" -> SymbolKind.TypeParameter;
			case "Operation" -> SymbolKind.Method;
			default ->
			{
				if (trimmed.startsWith("+") || trimmed.startsWith("-") || "reference".equals(trimmed))
				{
					yield SymbolKind.Field;
				}
				yield null;
			}
		};
	}

	private static PToken resolveNameToken(final List<PToken> tokens)
	{
		for (int i = 1; i < tokens.size(); i++)
		{
			final PToken tok = tokens.get(i);
			final String val = tok.value();

			if (val.startsWith("name=") && val.length() > "name=".length())
			{
				return new PToken(val.substring("name=".length()), tok.type(), tok.offset(), tok.length());
			}

			if ("name".equals(val))
			{
				if (i + 2 < tokens.size() && "=".equals(tokens.get(i + 1).value()))
				{
					final PToken candidate = tokens.get(i + 2);
					return new PToken(candidate.value(), candidate.type(), candidate.offset(), candidate.length());
				}
				if (i + 1 < tokens.size())
				{
					final PToken candidate = tokens.get(i + 1);
					return new PToken(candidate.value(), candidate.type(), candidate.offset(), candidate.length());
				}
			}

			final int eq = val.indexOf('=');
			if (eq > 0 && eq + 1 < val.length())
			{
				return new PToken(val.substring(eq + 1), tok.type(), tok.offset() + eq + 1, tok.length() - eq - 1);
			}
		}

		for (int i = 1; i < tokens.size(); i++)
		{
			final PToken tok = tokens.get(i);
			final String val = tok.value();
			if (val.isEmpty()) continue;
			if (!Character.isJavaIdentifierStart(val.charAt(0))) continue;
			if (val.startsWith("+") || val.startsWith("-")) continue;
			if (val.contains("=")) continue;
			if ("MetaModel".equals(val) ||
				"Group".equals(val) ||
				"Definition".equals(val) ||
				"Enum".equals(val) ||
				"Unit".equals(val) ||
				"Generic".equals(val) ||
				"Alias".equals(val) ||
				"JavaWrapper".equals(val) ||
				"includes".equals(val))
			{
				continue;
			}
			return tok;
		}

		return null;
	}

	private static Range rangeForNode(final PNode node, final CharSequence source)
	{
		final List<PToken> tokens = node.tokens();
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

		return rangeForOffsets(start, end, source);
	}

	private static Range rangeForToken(final PToken token, final CharSequence source)
	{
		final var span = TextPositions.spanOf(token, source);
		final int startLine = Math.max(0, span.line() - 1);
		final int startChar = Math.max(0, span.column() - 1);
		final int endLine = startLine;
		final int endChar = startChar + Math.max(1, span.length());

		final var start = new Position(startLine, startChar);
		final var end = new Position(endLine, endChar);
		return new Range(start, end);
	}

	private static Range rangeForOffsets(final int startOffset, final int endOffset, final CharSequence source)
	{
		final int startLine = Math.max(0, TextPositions.lineFor(source, startOffset) - 1);
		final int startChar = Math.max(0, TextPositions.columnFor(source, startOffset) - 1);
		final int endLine = Math.max(0, TextPositions.lineFor(source, endOffset) - 1);
		final int endChar = Math.max(0, TextPositions.columnFor(source, endOffset) - 1);

		final var start = new Position(startLine, startChar);
		final var end = new Position(endLine, endChar);
		return new Range(start, end);
	}

	private static Range spanToRange(final TextPositions.Span span, final CharSequence source)
	{
		final int startOffset = span.offset();
		final int endOffset = startOffset + Math.max(1, span.length());
		return rangeForOffsets(startOffset, endOffset, source);
	}
}
