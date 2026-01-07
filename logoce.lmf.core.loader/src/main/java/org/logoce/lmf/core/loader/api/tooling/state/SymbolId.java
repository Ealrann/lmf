package org.logoce.lmf.core.loader.api.tooling.state;

public record SymbolId(ModelKey modelKey,
					   LmSymbolKind kind,
					   String name,
					   String containerPath)
{
}
