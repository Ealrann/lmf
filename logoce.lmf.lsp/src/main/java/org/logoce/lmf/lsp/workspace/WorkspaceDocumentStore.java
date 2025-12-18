package org.logoce.lmf.lsp.workspace;

import org.logoce.lmf.lsp.HeaderTextScanner;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.WorkspaceIndex;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class WorkspaceDocumentStore
{
	private final WorkspaceIndex workspaceIndex;
	private final Map<URI, HeaderInfo> headersByUri = new HashMap<>();
	private final Map<String, Integer> requiredMetaModelsByCount = new HashMap<>();
	private long requiredMetaModelsGeneration;

	WorkspaceDocumentStore(final WorkspaceIndex workspaceIndex)
	{
		this.workspaceIndex = Objects.requireNonNull(workspaceIndex, "workspaceIndex");
	}

	DocumentChange openDocument(final URI uri, final int version, final String text)
	{
		final var state = new LmDocumentState(uri, version, text);
		workspaceIndex.putDocument(state);
		final boolean requiredChanged = updateHeader(uri, text);
		final boolean metaModelTouched = isMetaModelDocument(uri);
		return new DocumentChange(state, requiredChanged, metaModelTouched);
	}

	DocumentChange updateDocument(final URI uri, final int version, final String text)
	{
		final var existing = workspaceIndex.getDocument(uri);
		final var state = existing != null ? existing : new LmDocumentState(uri, version, "");
		state.setVersion(version);
		state.setText(text);
		if (existing == null)
		{
			workspaceIndex.putDocument(state);
		}

		final boolean requiredChanged = updateHeader(uri, text);
		final boolean metaModelTouched = isMetaModelDocument(uri);
		return new DocumentChange(state, requiredChanged, metaModelTouched);
	}

	DocumentChange closeDocument(final URI uri)
	{
		final var previous = headersByUri.get(uri);
		final boolean previousWasMetaModel = previous != null && previous.isMetaModelRoot();
		final boolean requiredChanged = removeHeader(uri);
		workspaceIndex.removeDocument(uri);
		return new DocumentChange(null, requiredChanged, previousWasMetaModel);
	}

	Set<String> requiredMetaModelNames()
	{
		return Set.copyOf(requiredMetaModelsByCount.keySet());
	}

	long requiredMetaModelsGeneration()
	{
		return requiredMetaModelsGeneration;
	}

	boolean isMetaModelDocument(final URI uri)
	{
		final var header = headersByUri.get(uri);
		return header != null && header.isMetaModelRoot();
	}

	void syncFromWorkspaceIndex()
	{
		final var previous = Set.copyOf(requiredMetaModelsByCount.keySet());

		headersByUri.clear();
		requiredMetaModelsByCount.clear();

		for (final var state : workspaceIndex.documents().values())
		{
			final var header = computeHeaderInfo(state.text());
			headersByUri.put(state.uri(), header);
			for (final var name : header.requiredMetaModels())
			{
				requiredMetaModelsByCount.merge(name, 1, Integer::sum);
			}
		}

		final var current = Set.copyOf(requiredMetaModelsByCount.keySet());
		if (!current.equals(previous))
		{
			requiredMetaModelsGeneration++;
		}
	}

	private boolean updateHeader(final URI uri, final String text)
	{
		final var previous = headersByUri.get(uri);
		final var current = computeHeaderInfo(text);
		headersByUri.put(uri, current);

		boolean changed = false;
		if (previous != null)
		{
			changed |= updateCounts(previous.requiredMetaModels(), current.requiredMetaModels());
		}
		else
		{
			changed |= updateCounts(Set.of(), current.requiredMetaModels());
		}

		if (changed)
		{
			requiredMetaModelsGeneration++;
		}
		return changed;
	}

	private boolean removeHeader(final URI uri)
	{
		final var previous = headersByUri.remove(uri);
		if (previous == null)
		{
			return false;
		}

		final boolean changed = updateCounts(previous.requiredMetaModels(), Set.of());
		if (changed)
		{
			requiredMetaModelsGeneration++;
		}
		return changed;
	}

	private boolean updateCounts(final Set<String> previous, final Set<String> current)
	{
		boolean unionChanged = false;

		for (final var name : previous)
		{
			if (current.contains(name))
			{
				continue;
			}
			final var count = requiredMetaModelsByCount.get(name);
			if (count == null)
			{
				continue;
			}
			if (count == 1)
			{
				requiredMetaModelsByCount.remove(name);
				unionChanged = true;
			}
			else
			{
				requiredMetaModelsByCount.put(name, count - 1);
			}
		}

		for (final var name : current)
		{
			if (previous.contains(name))
			{
				continue;
			}
			final var old = requiredMetaModelsByCount.putIfAbsent(name, 1);
			if (old == null)
			{
				unionChanged = true;
			}
			else
			{
				requiredMetaModelsByCount.put(name, old + 1);
			}
		}

		return unionChanged;
	}

	private static HeaderInfo computeHeaderInfo(final CharSequence text)
	{
		final boolean isMetaModelRoot = HeaderTextScanner.isMetaModelRoot(text);
		final var required = new HashSet<String>(HeaderTextScanner.parseMetamodelNames(text));
		final var qualifiedName = HeaderTextScanner.parseMetaModelQualifiedName(text);
		if (qualifiedName != null)
		{
			required.add(qualifiedName);
		}
		return new HeaderInfo(isMetaModelRoot, Set.copyOf(required));
	}

	record DocumentChange(LmDocumentState state, boolean requiredMetaModelsChanged, boolean metaModelDocumentTouched)
	{
	}

	private record HeaderInfo(boolean isMetaModelRoot, Set<String> requiredMetaModels)
	{
	}
}

