package org.logoce.lmf.cli.edit;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.diagnostics.ValidationReport;
import org.logoce.lmf.cli.json.JsonSerializers;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.remove.RemoveUnsetReference;
import org.logoce.lmf.cli.util.PathDisplay;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class EditJsonReportWriter
{
	private EditJsonReportWriter()
	{
	}

	public static void writeOutcome(final JsonWriter json, final CliContext context, final EditOutcome outcome)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(outcome, "outcome");

		final var plannedFiles = outcome.sources().keySet().stream().toList();
		final var writtenFiles = outcome.wrote() ? plannedFiles : List.<Path>of();

		json.name("outcome").beginObject()
			.name("changed").value(outcome.changed())
			.name("validationPassed").value(outcome.validationPassed())
			.name("wrote").value(outcome.wrote())
			.name("forcedWrite").value(outcome.forcedWrite())
			.name("plannedFileCount").value(plannedFiles.size())
			.name("plannedFiles").beginArray();
		for (final var path : plannedFiles)
		{
			json.value(PathDisplay.display(context.projectRoot(), path));
		}
		json.endArray()
			.name("writtenFileCount").value(writtenFiles.size())
			.name("writtenFiles").beginArray();
		for (final var path : writtenFiles)
		{
			json.value(PathDisplay.display(context.projectRoot(), path));
		}
		json.endArray()
			.endObject();
	}

	public static void writeDiagnostics(final JsonWriter json, final ValidationReport report)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(report, "report");

		json.name("diagnostics").beginArray();
		for (final var diagnostic : report.diagnostics())
		{
			JsonSerializers.writeLocatedDiagnostic(json, diagnostic.file(), diagnostic.diagnostic());
		}
		json.endArray();

		if (report.messages() != null && !report.messages().isEmpty())
		{
			json.name("messages").beginArray();
			for (final var message : report.messages())
			{
				json.value(message);
			}
			json.endArray();
		}
	}

	public static void writeUnsets(final JsonWriter json, final CliContext context, final List<RemoveUnsetReference> unsets)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(unsets, "unsets");

		json.name("unsets").beginArray();
		for (final var unset : unsets)
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
		json.endArray();
	}

	public static void writeReferenceRewrites(final JsonWriter json, final CliContext context, final List<ReferenceRewrite> rewrites)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(rewrites, "rewrites");

		json.name("referenceRewrites").beginArray();
		for (final var rewrite : rewrites)
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
		json.endArray();
	}

	public static void writeFiles(final JsonWriter json, final CliContext context, final List<Path> files, final String fieldName)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(files, "files");
		Objects.requireNonNull(fieldName, "fieldName");

		json.name(fieldName).beginArray();
		for (final var file : files)
		{
			json.value(PathDisplay.display(context.projectRoot(), file));
		}
		json.endArray();
	}
}
