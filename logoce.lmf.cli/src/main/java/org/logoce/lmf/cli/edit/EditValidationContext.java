package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public sealed interface EditValidationContext permits EditValidationContext.Workspace, EditValidationContext.SingleModel, CustomValidationContext
{
	boolean validate(Map<Path, String> sourcesByPath, PrintWriter err);

	record Workspace(RegistryService.PreparedWorkspace prepared,
					 Path projectRoot,
					 DocumentLoader documentLoader) implements EditValidationContext
	{
		public Workspace
		{
			Objects.requireNonNull(prepared, "prepared");
			Objects.requireNonNull(projectRoot, "projectRoot");
			Objects.requireNonNull(documentLoader, "documentLoader");
		}

		@Override
		public boolean validate(final Map<Path, String> sourcesByPath, final PrintWriter err)
		{
			Objects.requireNonNull(sourcesByPath, "sourcesByPath");
			Objects.requireNonNull(err, "err");

			final var validator = new org.logoce.lmf.cli.workspace.WorkspaceValidator();
			return validator.validate(prepared,
									  projectRoot.toAbsolutePath().normalize(),
									  sourcesByPath,
									  documentLoader,
									  err);
		}
	}

	record SingleModel(RegistryService.PreparedRegistry prepared,
					   Path projectRoot,
					   DocumentLoader documentLoader) implements EditValidationContext
	{
		public SingleModel
		{
			Objects.requireNonNull(prepared, "prepared");
			Objects.requireNonNull(projectRoot, "projectRoot");
			Objects.requireNonNull(documentLoader, "documentLoader");
		}

		@Override
		public boolean validate(final Map<Path, String> sourcesByPath, final PrintWriter err)
		{
			Objects.requireNonNull(sourcesByPath, "sourcesByPath");
			Objects.requireNonNull(err, "err");

			final var normalizedPath = prepared.targetPath().toAbsolutePath().normalize();
			final var source = sourcesByPath.getOrDefault(normalizedPath,
														  documentLoader.readString(normalizedPath, err));
			if (source == null)
			{
				return false;
			}

			final var doc = documentLoader.loadModelFromSource(prepared.registry(),
															   prepared.targetQualifiedName(),
															   source,
															   err);
			if (doc == null)
			{
				return false;
			}

			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				DiagnosticReporter.printDiagnostics(err,
													PathDisplay.display(projectRoot, normalizedPath),
													doc.diagnostics());
				return false;
			}

			return true;
		}
	}
}
