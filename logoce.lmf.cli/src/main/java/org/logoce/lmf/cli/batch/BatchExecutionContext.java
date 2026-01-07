package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.edit.WorkspaceEditApplier;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;
import org.logoce.lmf.cli.workspace.WorkspaceValidator;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record BatchExecutionContext(CliContext cliContext,
									BatchOptions options,
									Map<Path, String> overlaySources,
									DocumentLoader documentLoader,
									RegistryService registryService,
									WorkspaceDocumentsLoader documentsLoader,
									WorkspaceEditApplier editApplier,
									WorkspaceValidator validator,
									Set<BatchValidationRoot> validationRoots,
									List<String> deferredOut)
{
	public BatchExecutionContext
	{
		Objects.requireNonNull(cliContext, "cliContext");
		Objects.requireNonNull(options, "options");
		Objects.requireNonNull(overlaySources, "overlaySources");
		Objects.requireNonNull(documentLoader, "documentLoader");
		Objects.requireNonNull(registryService, "registryService");
		Objects.requireNonNull(documentsLoader, "documentsLoader");
		Objects.requireNonNull(editApplier, "editApplier");
		Objects.requireNonNull(validator, "validator");
		Objects.requireNonNull(validationRoots, "validationRoots");
		Objects.requireNonNull(deferredOut, "deferredOut");
	}

	public Path projectRoot()
	{
		return cliContext.projectRoot();
	}

	public PrintWriter out()
	{
		return cliContext.out();
	}

	public PrintWriter err()
	{
		return cliContext.err();
	}
}
