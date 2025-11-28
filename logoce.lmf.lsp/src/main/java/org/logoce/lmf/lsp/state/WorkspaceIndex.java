package org.logoce.lmf.lsp.state;

import org.logoce.lmf.model.util.ModelRegistry;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WorkspaceIndex
{
	private final Map<URI, LmDocumentState> documents = new ConcurrentHashMap<>();
	private volatile ModelRegistry modelRegistry = ModelRegistry.empty();

	// Global symbol and reference indices (M2-level for now).
	private final Map<SymbolId, SymbolEntry> symbolIndex = new ConcurrentHashMap<>();
	private final Map<URI, List<SymbolId>> symbolsByUri = new ConcurrentHashMap<>();

	private final Map<SymbolId, List<ReferenceOccurrence>> referenceIndex = new ConcurrentHashMap<>();
	private final Map<URI, List<ReferenceOccurrence>> referencesByUri = new ConcurrentHashMap<>();

	public Map<URI, LmDocumentState> documents()
	{
		return documents;
	}

	public ModelRegistry modelRegistry()
	{
		return modelRegistry;
	}

	public void setModelRegistry(final ModelRegistry modelRegistry)
	{
		this.modelRegistry = modelRegistry;
	}

	public LmDocumentState getDocument(final URI uri)
	{
		return documents.get(uri);
	}

	public void putDocument(final LmDocumentState state)
	{
		documents.put(state.uri(), state);
	}

	public void removeDocument(final URI uri)
	{
		documents.remove(uri);
		clearIndicesForDocument(uri);
	}

	public Map<SymbolId, SymbolEntry> symbolIndex()
	{
		return symbolIndex;
	}

	public Map<SymbolId, List<ReferenceOccurrence>> referenceIndex()
	{
		return referenceIndex;
	}

	public void clearIndicesForDocument(final URI uri)
	{
		final var symbolIds = symbolsByUri.remove(uri);
		if (symbolIds != null)
		{
			for (final var id : symbolIds)
			{
				symbolIndex.remove(id);
			}
		}

		final var refs = referencesByUri.remove(uri);
		if (refs != null)
		{
			for (final var ref : refs)
			{
				final var list = referenceIndex.get(ref.target());
				if (list != null)
				{
					list.removeIf(r -> r.uri().equals(uri) && r.range().equals(ref.range()));
					if (list.isEmpty())
					{
						referenceIndex.remove(ref.target());
					}
				}
			}
		}
	}

	public void registerSymbols(final URI uri, final List<SymbolEntry> entries)
	{
		clearIndicesForDocument(uri);

		if (entries.isEmpty())
		{
			return;
		}

		final var ids = new ArrayList<SymbolId>(entries.size());
		for (final var entry : entries)
		{
			symbolIndex.put(entry.id(), entry);
			ids.add(entry.id());
		}
		symbolsByUri.put(uri, ids);
	}

	public void registerReferences(final URI uri, final List<ReferenceOccurrence> refs)
	{
		// Remove old references for this URI from both per-uri and global maps.
		final var previous = referencesByUri.remove(uri);
		if (previous != null)
		{
			for (final var ref : previous)
			{
				final var list = referenceIndex.get(ref.target());
				if (list != null)
				{
					list.removeIf(r -> r.uri().equals(uri) && r.range().equals(ref.range()));
					if (list.isEmpty())
					{
						referenceIndex.remove(ref.target());
					}
				}
			}
		}

		if (refs.isEmpty())
		{
			return;
		}

		referencesByUri.put(uri, List.copyOf(refs));
		for (final var ref : refs)
		{
			referenceIndex.computeIfAbsent(ref.target(), k -> new ArrayList<>()).add(ref);
		}
	}

	public List<ReferenceOccurrence> referencesForUri(final URI uri)
	{
		final var refs = referencesByUri.get(uri);
		return refs == null ? List.of() : refs;
	}

	public List<SymbolEntry> symbolsForUri(final URI uri)
	{
		final var ids = symbolsByUri.get(uri);
		if (ids == null || ids.isEmpty())
		{
			return List.of();
		}
		final var entries = new ArrayList<SymbolEntry>(ids.size());
		for (final var id : ids)
		{
			final var entry = symbolIndex.get(id);
			if (entry != null)
			{
				entries.add(entry);
			}
		}
		return List.copyOf(entries);
	}
}
