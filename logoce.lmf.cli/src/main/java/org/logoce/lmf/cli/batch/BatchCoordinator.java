package org.logoce.lmf.cli.batch;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.json.JsonSerializers;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.cli.edit.WorkspaceEditApplier;
import org.logoce.lmf.cli.workspace.DocumentLoader;
import org.logoce.lmf.cli.workspace.RegistryService;
import org.logoce.lmf.cli.workspace.WorkspaceDocumentsLoader;
import org.logoce.lmf.cli.workspace.WorkspaceValidator;

import java.io.PrintWriter;
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
			if (options.json())
			{
				writeJsonEmpty(context, options);
			}
			else
			{
				context.out().println("OK: no batch operations");
			}
			return ExitCodes.OK;
		}

		final var reportContext = options.json() ? silentContext(context) : context;
		final var overlaySources = new LinkedHashMap<Path, String>();
		final var documentLoader = new DocumentLoader(overlaySources);
		final var registryService = new RegistryService(reportContext.projectRoot(), documentLoader);
		final var documentsLoader = new WorkspaceDocumentsLoader();
		final var editApplier = new WorkspaceEditApplier();
		final var validator = new WorkspaceValidator();
		final var deferredOut = new ArrayList<String>();
		final var validationRoots = new LinkedHashSet<BatchValidationRoot>();

		final var executionContext = new BatchExecutionContext(reportContext,
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
		final var opReports = new ArrayList<BatchOperationReport>(operations.size());

		for (final var operation : operations)
		{
			final var result = executor.execute(operation);
			if (result.report() != null)
			{
				opReports.add(result.report());
			}
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

		final var finalization = new BatchFinalizer().finalizeBatch(executionContext,
											anyHardFailure,
											anyValidationFailure,
											maxExitCode);
		if (options.json())
		{
			writeJsonResult(context, options, opReports, finalization);
		}
		return finalization.exitCode();
	}

	private static CliContext silentContext(final CliContext context)
	{
		final var sink = new PrintWriter(java.io.Writer.nullWriter());
		return new CliContext(context.projectRoot(), sink, context.err());
	}

	private static void writeJsonEmpty(final CliContext context, final BatchOptions options)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("batch")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("options").beginObject();
		writeJsonOptions(json, options);
		json.endObject()
			.name("operations").beginArray()
			.endArray()
			.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
	}

	private static void writeJsonResult(final CliContext context,
										final BatchOptions options,
										final List<BatchOperationReport> operationReports,
										final BatchFinalizationResult finalization)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("batch")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("options").beginObject();
		writeJsonOptions(json, options);
		json.endObject()
			.name("operations").beginArray();

		for (final var report : operationReports)
		{
			json.beginObject()
				.name("index").value(report.index())
				.name("lineNumber").value(report.lineNumber())
				.name("command").value(report.command())
				.name("args").beginArray();
			for (final var arg : report.args())
			{
				json.value(arg);
			}
			json.endArray()
				.name("status").value(report.status())
				.name("message").value(report.message())
				.name("exitCode").value(report.exitCode())
				.name("validationFailed").value(report.validationFailed())
				.name("stagedFiles").beginArray();
			for (final var file : report.stagedFiles())
			{
				json.value(PathDisplay.display(context.projectRoot(), file));
			}
			json.endArray()
				.name("unsets").beginArray();
			for (final var unset : report.unsets())
			{
				json.beginObject()
					.name("file").value(PathDisplay.display(context.projectRoot(), unset.path()))
					.name("rawReference").value(unset.raw())
					.name("resolved").beginObject()
					.name("modelQualifiedName").value(unset.targetId().modelQualifiedName())
					.name("path").value(unset.targetId().path())
					.endObject();
				if (unset.span() != null)
				{
					json.name("span");
					JsonSerializers.writeSpan(json, unset.span());
				}
				json.endObject();
			}
			json.endArray()
				.name("referenceRewrites").beginArray();
			for (final var rewrite : report.rewrites())
			{
				json.beginObject()
					.name("file").value(PathDisplay.display(context.projectRoot(), rewrite.path()))
					.name("oldRaw").value(rewrite.oldRaw())
					.name("newRaw").value(rewrite.newRaw())
					.name("resolvedTarget").beginObject()
					.name("modelQualifiedName").value(rewrite.resolvedTarget().modelQualifiedName())
					.name("path").value(rewrite.resolvedTarget().path())
					.endObject();
				if (rewrite.span() != null)
				{
					json.name("span");
					JsonSerializers.writeSpan(json, rewrite.span());
				}
				json.endObject();
			}
			json.endArray()
				.name("diagnostics").beginArray();
			for (final var diagnostic : report.diagnostics())
			{
				JsonSerializers.writeLocatedDiagnostic(json, diagnostic.file(), diagnostic.diagnostic());
			}
			json.endArray()
				.endObject();
		}

		json.endArray()
			.name("finalization").beginObject()
			.name("plannedFileCount").value(finalization.stagedFileCount())
			.name("plannedFiles").beginArray();
		for (final var file : finalization.files())
		{
			json.value(PathDisplay.display(context.projectRoot(), file));
		}
		json.endArray()
			.name("writtenFileCount").value(finalization.wrote() ? finalization.files().size() : 0)
			.name("writtenFiles").beginArray();
		for (final var file : finalization.wrote() ? finalization.files() : java.util.List.<java.nio.file.Path>of())
		{
			json.value(PathDisplay.display(context.projectRoot(), file));
		}
		json.endArray()
			.name("dryRun").value(finalization.dryRun())
			.name("wrote").value(finalization.wrote())
			.name("forcedWrite").value(finalization.forcedWrite())
			.name("validationFailed").value(finalization.validationFailed())
			.name("exitCode").value(finalization.exitCode())
			.endObject()
			.name("ok").value(finalization.exitCode() == ExitCodes.OK)
			.name("exitCode").value(finalization.exitCode())
			.endObject()
			.flush();
		context.out().println();
	}

	private static void writeJsonOptions(final JsonWriter json, final BatchOptions options)
	{
		json.name("dryRun").value(options.dryRun())
			.name("continueOnError").value(options.continueOnError())
			.name("force").value(options.force())
			.name("validateMode").value(options.validateMode().name().toLowerCase(java.util.Locale.ROOT))
			.name("defaultModel").value(options.defaultModel())
			.name("json").value(options.json());
		if (options.file() != null)
		{
			json.name("file").value(options.file().toString());
		}
		json.name("readFromStdin").value(options.readFromStdin());
	}
}
