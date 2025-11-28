package org.logoce.lmf.lsp;

/**
 * Simple configuration flags for the LMF LSP server.
 * Values are typically driven by workspace/didChangeConfiguration.
 */
public record Settings(boolean experimentalRename,
					   boolean experimentalInstanceModels,
					   boolean formattingEnabled,
					   boolean advancedCompletion)
{
	public static Settings defaults()
	{
		return new Settings(true, false, false, false);
	}
}

