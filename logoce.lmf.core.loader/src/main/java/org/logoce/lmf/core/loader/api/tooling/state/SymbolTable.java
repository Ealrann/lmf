package org.logoce.lmf.core.loader.api.tooling.state;

import java.util.List;

public final class SymbolTable
{
	public static final SymbolTable EMPTY = new SymbolTable(List.of());

	private final List<SymbolEntry> entries;

	public SymbolTable(final List<SymbolEntry> entries)
	{
		this.entries = List.copyOf(entries);
	}

	public List<SymbolEntry> entries()
	{
		return entries;
	}
}
