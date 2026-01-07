package org.logoce.lmf.lsp.workspace;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.tooling.state.LmDocumentState;
import org.logoce.lmf.core.loader.api.tooling.state.SemanticSnapshot;
import org.logoce.lmf.core.loader.api.tooling.state.SyntaxSnapshot;

import java.util.ArrayList;
import java.util.function.Supplier;

final class DiagnosticsPublisher
{
	private final Supplier<LanguageClient> clientSupplier;

	DiagnosticsPublisher(final Supplier<LanguageClient> clientSupplier)
	{
		this.clientSupplier = clientSupplier;
	}

	void publish(final LmDocumentState state)
	{
		final LanguageClient client = clientSupplier.get();
		if (client == null)
		{
			return;
		}

		final var diagnostics = new ArrayList<Diagnostic>();

		final SyntaxSnapshot syntax = state.syntaxSnapshot();
		if (syntax != null)
		{
			for (final LmDiagnostic d : syntax.diagnostics())
			{
				diagnostics.add(toLspDiagnostic(d));
			}
		}

		final SemanticSnapshot semantic = state.semanticSnapshot();
		if (semantic != null)
		{
			for (final LmDiagnostic d : semantic.diagnostics())
			{
				diagnostics.add(toLspDiagnostic(d));
			}
		}

		final var params = new PublishDiagnosticsParams(state.uri().toString(), diagnostics);
		client.publishDiagnostics(params);
	}

	private static Diagnostic toLspDiagnostic(final LmDiagnostic d)
	{
		final int line = Math.max(0, d.line() - 1);
		final int startChar = Math.max(0, d.column() - 1);
		final int endChar = startChar + Math.max(1, d.length());

		final var start = new Position(line, startChar);
		final var end = new Position(line, endChar);
		final Range range = new Range(start, end);

		final var diag = new Diagnostic();
		diag.setRange(range);
		diag.setMessage(d.message());
		diag.setSeverity(switch (d.severity())
		{
			case ERROR -> DiagnosticSeverity.Error;
			case WARNING -> DiagnosticSeverity.Warning;
			case INFO -> DiagnosticSeverity.Information;
		});
		return diag;
	}
}
