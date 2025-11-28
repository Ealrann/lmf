package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class LmWorkspaceService implements WorkspaceService
{
	private static final Logger LOG = LoggerFactory.getLogger(LmWorkspaceService.class);

	private final LmLanguageServer server;

	public LmWorkspaceService(final LmLanguageServer server)
	{
		this.server = server;
	}

	@Override
	public void didChangeConfiguration(final DidChangeConfigurationParams params)
	{
		final Object settingsObj = params.getSettings();
		if (!(settingsObj instanceof Map<?, ?> root))
		{
			return;
		}

		Settings current = server.settings();

		boolean experimentalRename = current.experimentalRename();
		boolean experimentalInstanceModels = current.experimentalInstanceModels();
		boolean formattingEnabled = current.formattingEnabled();
		boolean advancedCompletion = current.advancedCompletion();

		try
		{
			// Expect structure like: { "lm": { "lsp": { ...flags... } } }
			final Object lmNode = root.get("lm");
			if (lmNode instanceof Map<?, ?> lm)
			{
				final Object lspNode = lm.get("lsp");
				if (lspNode instanceof Map<?, ?> lsp)
				{
					experimentalRename = readBoolean(lsp, "experimentalRename", experimentalRename);
					experimentalInstanceModels = readBoolean(lsp, "experimentalInstanceModels", experimentalInstanceModels);
					formattingEnabled = readBoolean(lsp, "formattingEnabled", formattingEnabled);
					advancedCompletion = readBoolean(lsp, "advancedCompletion", advancedCompletion);
				}
			}
		}
		catch (Exception e)
		{
			LOG.warn("Failed to parse settings from didChangeConfiguration", e);
			return;
		}

		server.updateSettings(new Settings(experimentalRename, experimentalInstanceModels, formattingEnabled, advancedCompletion));
	}

	@SuppressWarnings("unchecked")
	private static boolean readBoolean(final Map<?, ?> map, final String key, final boolean defaultValue)
	{
		final Object value = map.get(key);
		if (value instanceof Boolean b)
		{
			return b;
		}
		return defaultValue;
	}

	@Override
	public void didChangeWatchedFiles(final DidChangeWatchedFilesParams params)
	{
		// File system events can be wired to workspace re-indexing in later steps.
	}
}
