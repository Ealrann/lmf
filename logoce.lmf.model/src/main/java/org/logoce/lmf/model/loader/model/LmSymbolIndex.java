package org.logoce.lmf.model.loader.model;

import org.logoce.lmf.model.util.TextPositions;

import java.util.List;

/**
 * LSP-agnostic symbol index for a single LM document.
 * <p>
 * Declarations are keyed by {@link SymbolId} and carry a {@link TextPositions.Span}.
 * References point to a {@link SymbolId} and a span in the same document.
 */
public record LmSymbolIndex(List<SymbolSpan> declarations,
							List<ReferenceSpan> references)
{
	public enum SymbolKind
	{
		META_MODEL,
		TYPE,
		FEATURE
	}

	public record SymbolId(String modelDomain, String modelName, SymbolKind kind, String name)
	{
	}

	public record SymbolSpan(SymbolId id, TextPositions.Span span, SymbolId container)
	{
	}

	public record ReferenceSpan(SymbolId target, TextPositions.Span span)
	{
	}
}
