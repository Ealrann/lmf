package org.logoce.lmf.cli.json;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.diagnostics.ValidationReport;

import java.util.List;
import java.util.Objects;

public final class JsonErrorWriter
{
	private JsonErrorWriter()
	{
	}

	public static void writeError(final CliContext context,
								  final String command,
								  final int exitCode,
								  final String message)
	{
		writeError(context, command, exitCode, message, List.of());
	}

	public static void writeError(final CliContext context,
								  final String command,
								  final int exitCode,
								  final String message,
								  final List<String> details)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(command, "command");
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(details, "details");

		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value(command)
			.name("ok").value(false)
			.name("exitCode").value(exitCode)
			.name("error").beginObject()
			.name("message").value(message)
			.name("details").beginArray();
		for (final var detail : details)
		{
			json.value(detail);
		}
		json.endArray()
			.endObject()
			.endObject()
			.flush();
		context.out().println();
	}

	public static void writeError(final CliContext context,
								  final String command,
								  final int exitCode,
								  final String message,
								  final ValidationReport report)
	{
		writeError(context, command, exitCode, message, List.of(), report);
	}

	public static void writeError(final CliContext context,
								  final String command,
								  final int exitCode,
								  final String message,
								  final List<String> details,
								  final ValidationReport report)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(command, "command");
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(details, "details");
		Objects.requireNonNull(report, "report");

		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value(command)
			.name("ok").value(false)
			.name("exitCode").value(exitCode)
			.name("error").beginObject()
			.name("message").value(message)
			.name("details").beginArray();
		for (final var detail : details)
		{
			json.value(detail);
		}
		json.endArray()
			.endObject()
			.name("diagnostics").beginArray();
		for (final var diagnostic : report.diagnostics())
		{
			JsonSerializers.writeLocatedDiagnostic(json, diagnostic.file(), diagnostic.diagnostic());
		}
		json.endArray();
		if (report.messages() != null && !report.messages().isEmpty())
		{
			json.name("messages").beginArray();
			for (final var msg : report.messages())
			{
				json.value(msg);
			}
			json.endArray();
		}
		json.endObject()
			.flush();
		context.out().println();
	}
}
