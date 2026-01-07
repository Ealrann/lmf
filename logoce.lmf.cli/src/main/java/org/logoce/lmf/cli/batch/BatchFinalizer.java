package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.edit.WorkspaceWriteTransaction;
import org.logoce.lmf.cli.format.LmSourceFormatter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceValidator;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class BatchFinalizer
{
	public int finalizeBatch(final BatchExecutionContext context,
							final boolean anyHardFailure,
							boolean anyValidationFailure,
							final int maxExitCode)
	{
		Objects.requireNonNull(context, "context");

		final var err = context.err();
		final var out = context.out();
		final var overlaySources = context.overlaySources();
		final var options = context.options();

		if (anyHardFailure)
		{
			err.println("Batch failed; no changes written");
			return maxExitCode;
		}

		if (!overlaySources.isEmpty())
		{
			final var formattedOverlay = new LmSourceFormatter().formatAll(overlaySources);
			overlaySources.clear();
			overlaySources.putAll(formattedOverlay);
		}

		if (options.validateMode() == BatchOptions.ValidateMode.FINAL)
		{
			anyValidationFailure |= !validateFinal(context,
											overlaySources,
											context.documentLoader(),
											context.registryService(),
											context.validator(),
											context.validationRoots());
		}

		if (options.dryRun())
		{
			out.println("DRY-RUN: staged " + overlaySources.size() + " file(s); no changes written");
			for (final var line : context.deferredOut())
			{
				out.println(line);
			}
			return anyValidationFailure ? ExitCodes.INVALID : ExitCodes.OK;
		}

		if (overlaySources.isEmpty())
		{
			out.println("OK: no changes to write");
			return ExitCodes.OK;
		}

		if (anyValidationFailure && options.validateMode() != BatchOptions.ValidateMode.NONE && !options.force())
		{
			err.println("Batch produced validation errors; no changes written");
			return ExitCodes.INVALID;
		}

		final var transaction = new WorkspaceWriteTransaction();
		for (final var entry : overlaySources.entrySet())
		{
			transaction.put(entry.getKey(), entry.getValue());
		}

		if (!transaction.commit(err))
		{
			err.println("Batch failed; no changes written");
			return ExitCodes.INVALID;
		}

		for (final var line : context.deferredOut())
		{
			out.println(line);
		}

		if (anyValidationFailure)
		{
			err.println("FORCED: wrote changes despite validation errors");
			return ExitCodes.INVALID;
		}

		out.println("OK: updated " + overlaySources.size() + " file(s)");
		return ExitCodes.OK;
	}

	private static boolean validateFinal(final BatchExecutionContext context,
									final Map<Path, String> overlaySources,
									final DocumentLoader documentLoader,
									final RegistryService registryService,
									final WorkspaceValidator validator,
									final Set<BatchValidationRoot> validationRoots)
	{
		boolean ok = true;
		for (final var root : validationRoots)
		{
			if (root.includeImporters())
			{
				final var prepareResult = registryService.prepareForModelAndImporters(root.modelPath(), context.err(), true);
				if (prepareResult instanceof RegistryService.PrepareWorkspaceResult.Failure)
				{
					ok = false;
					continue;
				}

				final var prepared = ((RegistryService.PrepareWorkspaceResult.Success) prepareResult).workspace();
				ok &= validator.validate(prepared,
									context.projectRoot(),
									overlaySources,
									documentLoader,
									context.err());
			}
			else
			{
				ok &= validateModelOnly(context, root.modelPath(), overlaySources, documentLoader, registryService);
			}
		}
		return ok;
	}

	private static boolean validateModelOnly(final BatchExecutionContext context,
									final Path modelPath,
									final Map<Path, String> overlaySources,
									final DocumentLoader documentLoader,
									final RegistryService registryService)
	{
		final var prepareResult = registryService.prepareForModel(modelPath, context.err());
		if (prepareResult instanceof RegistryService.PrepareResult.Failure)
		{
			return false;
		}

		final var prepared = ((RegistryService.PrepareResult.Success) prepareResult).registry();
		final var registry = prepared.registry();
		final var targetQualifiedName = prepared.targetQualifiedName();

		final var source = overlaySources.getOrDefault(modelPath.toAbsolutePath().normalize(),
												documentLoader.readString(modelPath, context.err()));
		if (source == null)
		{
			return false;
		}

		final var doc = documentLoader.loadModelFromSource(registry, targetQualifiedName, source, context.err());
		if (doc == null)
		{
			return false;
		}

		if (DiagnosticReporter.hasErrors(doc.diagnostics()))
		{
			DiagnosticReporter.printDiagnostics(context.err(),
									PathDisplay.display(context.projectRoot(), modelPath),
									doc.diagnostics());
			return false;
		}

		return true;
	}
}
