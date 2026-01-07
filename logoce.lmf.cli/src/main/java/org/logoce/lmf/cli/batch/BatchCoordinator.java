package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.edit.WorkspaceEditApplier;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;
import org.logoce.lmf.cli.workspace.WorkspaceValidator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class BatchCoordinator
{
	public int run(final CliContext context, final BatchOptions options, final List<BatchOperation> operations)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(options, "options");
		Objects.requireNonNull(operations, "operations");

		if (operations.isEmpty())
		{
			context.out().println("OK: no batch operations");
			return ExitCodes.OK;
		}

		final var overlaySources = new LinkedHashMap<Path, String>();
		final var documentLoader = new DocumentLoader(overlaySources);
		final var registryService = new RegistryService(context.projectRoot(), documentLoader);
		final var documentsLoader = new WorkspaceDocumentsLoader();
		final var editApplier = new WorkspaceEditApplier();
		final var validator = new WorkspaceValidator();
		final var deferredOut = new ArrayList<String>();
		final var validationRoots = new LinkedHashSet<BatchValidationRoot>();

		final var executionContext = new BatchExecutionContext(context,
												options,
												overlaySources,
												documentLoader,
												registryService,
												documentsLoader,
												editApplier,
												validator,
												validationRoots,
												deferredOut);

		final var executor = new BatchOperationExecutor(executionContext);
		boolean anyHardFailure = false;
		boolean anyValidationFailure = false;
		int maxExitCode = ExitCodes.OK;

		for (final var operation : operations)
		{
			final var result = executor.execute(operation);
			if (result.exitCode() != ExitCodes.OK)
			{
				anyHardFailure = true;
				maxExitCode = Math.max(maxExitCode, result.exitCode());
				if (!options.continueOnError())
				{
					break;
				}
				continue;
			}

			anyValidationFailure |= result.validationFailed();
		}

		return new BatchFinalizer().finalizeBatch(executionContext,
											anyHardFailure,
											anyValidationFailure,
											maxExitCode);
	}
}
