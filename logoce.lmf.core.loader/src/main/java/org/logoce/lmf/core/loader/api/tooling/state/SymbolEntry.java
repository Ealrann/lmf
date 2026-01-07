package org.logoce.lmf.core.loader.api.tooling.state;

import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.net.URI;

public record SymbolEntry(SymbolId id, SymbolId container, URI uri, TextPositions.Span span)
{
}
