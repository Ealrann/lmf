package org.logoce.lmf.lsp.state;

import org.eclipse.lsp4j.Range;

import java.net.URI;

public record SymbolEntry(SymbolId id, URI uri, Range range)
{
}

