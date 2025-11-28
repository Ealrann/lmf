package org.logoce.lmf.lsp.state;

import org.eclipse.lsp4j.Range;

import java.net.URI;

public record ReferenceOccurrence(SymbolId target, URI uri, Range range)
{
}

