package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.diagnostics.ValidationReport;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public sealed interface EditValidationContext permits EditValidationContext.Workspace, EditValidationContext.SingleModel, CustomValidationContext
{
	ValidationReport validate(Map<Path, String> sourcesByPath, PrintWriter err);

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
		public ValidationReport validate(final Map<Path, String> sourcesByPath, final PrintWriter err)
		{
			Objects.requireNonNull(sourcesByPath, "sourcesByPath");
			Objects.requireNonNull(err, "err");

			final var validator = new org.logoce.lmf.cli.workspace.WorkspaceValidator();
			return validator.validateWithReport(prepared,
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
		public ValidationReport validate(final Map<Path, String> sourcesByPath, final PrintWriter err)
		{
			Objects.requireNonNull(sourcesByPath, "sourcesByPath");
			Objects.requireNonNull(err, "err");

			final var normalizedPath = prepared.targetPath().toAbsolutePath().normalize();
			final var source = sourcesByPath.getOrDefault(normalizedPath,
														  documentLoader.readString(normalizedPath, err));
			if (source == null)
			{
				return new ValidationReport(false,
											List.of(),
											List.of("Failed to read model source: " + PathDisplay.display(projectRoot, normalizedPath)));
			}

			final var doc = documentLoader.loadModelFromSource(prepared.registry(),
															   prepared.targetQualifiedName(),
															   source,
															   err);
			if (doc == null)
			{
				return new ValidationReport(false,
											List.of(),
											List.of("Failed to load model: " + PathDisplay.display(projectRoot, normalizedPath)));
			}

			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				DiagnosticReporter.printDiagnostics(err,
													PathDisplay.display(projectRoot, normalizedPath),
													doc.diagnostics());
				final var diagnostics = new ArrayList<DiagnosticItem>();
				final var file = PathDisplay.display(projectRoot, normalizedPath);
				for (final var diagnostic : doc.diagnostics())
				{
					diagnostics.add(new DiagnosticItem(file, diagnostic));
				}
				return new ValidationReport(false, List.copyOf(diagnostics), List.of());
			}

			return ValidationReport.success();
		}
	}
}
