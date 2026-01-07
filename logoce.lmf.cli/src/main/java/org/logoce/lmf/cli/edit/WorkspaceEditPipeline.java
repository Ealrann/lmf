package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.format.LmSourceFormatter;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorkspaceEditPipeline
{
	private final WorkspaceEditApplier editApplier = new WorkspaceEditApplier();
	private final LmSourceFormatter formatter = new LmSourceFormatter();

	public EditOutcome applyEdits(final EditValidationContext validationContext,
								  final Map<Path, List<TextEdits.TextEdit>> editsByFile,
								  final Map<Path, String> sourcesByPath,
								  final EditOptions options,
								  final PrintWriter err)
	{
		Objects.requireNonNull(validationContext, "validationContext");
		Objects.requireNonNull(editsByFile, "editsByFile");
		Objects.requireNonNull(sourcesByPath, "sourcesByPath");
		Objects.requireNonNull(options, "options");
		Objects.requireNonNull(err, "err");

		final var updatedSources = editApplier.apply(editsByFile, sourcesByPath);
		return processSources(validationContext, updatedSources, options, err);
	}

	public EditOutcome processSources(final EditValidationContext validationContext,
									  final Map<Path, String> updatedSources,
									  final EditOptions options,
									  final PrintWriter err)
	{
		Objects.requireNonNull(validationContext, "validationContext");
		Objects.requireNonNull(updatedSources, "updatedSources");
		Objects.requireNonNull(options, "options");
		Objects.requireNonNull(err, "err");

		if (updatedSources.isEmpty())
		{
			return EditOutcome.noChanges();
		}

		final var formattedSources = options.format()
											? formatter.formatAll(updatedSources)
											: Map.copyOf(updatedSources);

		final boolean validationPassed = !options.validate() || validationContext.validate(formattedSources, err);
		final boolean canCommit = options.commit() && (validationPassed || options.force());
		final boolean wrote = canCommit && commit(formattedSources, err);
		final boolean forcedWrite = wrote && !validationPassed && options.force();

		return new EditOutcome(formattedSources, validationPassed, wrote, forcedWrite);
	}

	private static boolean commit(final Map<Path, String> sourcesByPath, final PrintWriter err)
	{
		if (sourcesByPath == null || sourcesByPath.isEmpty())
		{
			return false;
		}

		final var transaction = new WorkspaceWriteTransaction();
		for (final var entry : sourcesByPath.entrySet())
		{
			transaction.put(entry.getKey(), entry.getValue());
		}

		return transaction.commit(err);
	}
}

