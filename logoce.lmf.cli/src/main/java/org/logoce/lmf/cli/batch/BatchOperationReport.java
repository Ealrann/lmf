package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.edit.ReferenceRewrite;
import org.logoce.lmf.cli.remove.RemoveUnsetReference;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record BatchOperationReport(int index,
								   int lineNumber,
								   String command,
								   List<String> args,
								   String status,
								   String message,
								   int exitCode,
								   boolean validationFailed,
								   List<Path> stagedFiles,
								   List<DiagnosticItem> diagnostics,
								   List<RemoveUnsetReference> unsets,
								   List<ReferenceRewrite> rewrites)
{
	public BatchOperationReport
	{
		Objects.requireNonNull(command, "command");
		Objects.requireNonNull(args, "args");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(message, "message");
		stagedFiles = stagedFiles == null ? List.of() : List.copyOf(stagedFiles);
		diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
		unsets = unsets == null ? List.of() : List.copyOf(unsets);
		rewrites = rewrites == null ? List.of() : List.copyOf(rewrites);
	}
}
