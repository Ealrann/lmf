package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class NoopClient implements LanguageClient
{
	@Override
	public void telemetryEvent(final Object object)
	{
	}

	@Override
	public void publishDiagnostics(final PublishDiagnosticsParams diagnostics)
	{
	}

	@Override
	public void showMessage(final MessageParams messageParams)
	{
	}

	@Override
	public CompletableFuture<MessageActionItem> showMessageRequest(final ShowMessageRequestParams requestParams)
	{
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void logMessage(final MessageParams message)
	{
	}

	@Override
	public CompletableFuture<List<WorkspaceFolder>> workspaceFolders()
	{
		return CompletableFuture.completedFuture(List.of());
	}

	@Override
	public CompletableFuture<List<Object>> configuration(final ConfigurationParams configurationParams)
	{
		return CompletableFuture.completedFuture(List.of());
	}

}
