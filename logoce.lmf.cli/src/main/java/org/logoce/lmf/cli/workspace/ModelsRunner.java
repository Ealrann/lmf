package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.CliContext;
import org.logoce.lmf.cli.ExitCodes;
import org.logoce.lmf.cli.json.JsonErrorWriter;
import org.logoce.lmf.cli.json.JsonWriter;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ModelsRunner
{
	public record Options(boolean duplicatesOnly, boolean json)
	{
	}

	public int run(final CliContext context, final Options options)
	{
		Objects.requireNonNull(context, "context");
		Objects.requireNonNull(options, "options");

		final var projectRoot = context.projectRoot();
		final var out = context.out();
		final var err = context.err();

		final var index = new DiskModelHeaderIndex();
		try
		{
			index.refresh(projectRoot);
		}
		catch (Exception e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			if (options.json())
			{
				JsonErrorWriter.writeError(context, "models", ExitCodes.INVALID, "Failed to scan workspace: " + message);
			}
			else
			{
				err.println("Failed to scan workspace: " + message);
			}
			return ExitCodes.INVALID;
		}

		final var headers = index.headers()
								 .stream()
								 .sorted(Comparator.comparing(h -> PathDisplay.display(projectRoot, h.path())))
								 .toList();

		if (options.duplicatesOnly())
		{
			final var duplicates = computeDuplicates(headers);
			return options.json()
				   ? printDuplicatesJson(context, duplicates)
				   : printDuplicatesText(projectRoot, out, duplicates);
		}

		return options.json()
			   ? printAllJson(context, headers)
			   : printAllText(projectRoot, out, headers);
	}

	private static int printAllText(final Path projectRoot,
									final PrintWriter out,
									final List<DiskModelHeaderIndex.DiskModelHeader> headers)
	{
		for (final var header : headers)
		{
			final var path = PathDisplay.display(projectRoot, header.path());
			final var kind = header.metaModelRoot() ? "MetaModel" : "Model";
			final var qualifiedName = header.qualifiedName() == null ? "" : header.qualifiedName();
			out.println(path + "\t" + kind + "\t" + qualifiedName);
		}
		return ExitCodes.OK;
	}

	private static int printAllJson(final CliContext context,
									final List<DiskModelHeaderIndex.DiskModelHeader> headers)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("models")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("options").beginObject()
			.name("duplicatesOnly").value(false)
			.name("json").value(true)
			.endObject()
			.name("modelCount").value(headers.size())
			.name("models").beginArray();

		for (final var header : headers)
		{
			json.beginObject()
				.name("path").value(PathDisplay.display(context.projectRoot(), header.path()))
				.name("fileName").value(header.path() == null || header.path().getFileName() == null ? "" : header.path().getFileName().toString())
				.name("metaModelRoot").value(header.metaModelRoot())
				.name("domain").value(header.domain())
				.name("name").value(header.name())
				.name("qualifiedName").value(header.qualifiedName())
				.name("imports").beginArray();
			for (final var imp : header.imports())
			{
				json.value(imp);
			}
			json.endArray()
				.name("metamodels").beginArray();
			for (final var mm : header.metamodels())
			{
				json.value(mm);
			}
			json.endArray()
				.endObject();
		}

		json.endArray()
			.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
		return ExitCodes.OK;
	}

	private static int printDuplicatesText(final Path projectRoot,
										  final PrintWriter out,
										  final Duplicates duplicates)
	{
		out.println("Duplicate file names (" + duplicates.duplicateFileNames().size() + "):");
		for (final var entry : duplicates.duplicateFileNames())
		{
			out.println(entry.key() + "\t" + entry.items().size());
			for (final var item : entry.items())
			{
				out.println("  " + PathDisplay.display(projectRoot, item.path()) + "\t" + safe(item.qualifiedName()));
			}
		}

		out.println("Duplicate qualified names (" + duplicates.duplicateQualifiedNames().size() + "):");
		for (final var entry : duplicates.duplicateQualifiedNames())
		{
			out.println(entry.key() + "\t" + entry.items().size());
			for (final var item : entry.items())
			{
				out.println("  " + PathDisplay.display(projectRoot, item.path()));
			}
		}

		return ExitCodes.OK;
	}

	private static int printDuplicatesJson(final CliContext context, final Duplicates duplicates)
	{
		final var json = new JsonWriter(context.out());
		json.beginObject()
			.name("command").value("models")
			.name("projectRoot").value(context.projectRoot().toString())
			.name("options").beginObject()
			.name("duplicatesOnly").value(true)
			.name("json").value(true)
			.endObject();

		json.name("duplicateFileNames").beginArray();
		for (final var entry : duplicates.duplicateFileNames())
		{
			json.beginObject()
				.name("fileName").value(entry.key())
				.name("count").value(entry.items().size())
				.name("items").beginArray();
			for (final var item : entry.items())
			{
				json.beginObject()
					.name("path").value(PathDisplay.display(context.projectRoot(), item.path()))
					.name("qualifiedName").value(item.qualifiedName())
					.endObject();
			}
			json.endArray()
				.endObject();
		}
		json.endArray();

		json.name("duplicateQualifiedNames").beginArray();
		for (final var entry : duplicates.duplicateQualifiedNames())
		{
			json.beginObject()
				.name("qualifiedName").value(entry.key())
				.name("count").value(entry.items().size())
				.name("paths").beginArray();
			for (final var item : entry.items())
			{
				json.value(PathDisplay.display(context.projectRoot(), item.path()));
			}
			json.endArray()
				.endObject();
		}
		json.endArray()
			.name("ok").value(true)
			.name("exitCode").value(ExitCodes.OK)
			.endObject()
			.flush();
		context.out().println();
		return ExitCodes.OK;
	}

	private static Duplicates computeDuplicates(final List<DiskModelHeaderIndex.DiskModelHeader> headers)
	{
		final var byFileName = new LinkedHashMap<String, List<Item>>();
		final var byQualifiedName = new LinkedHashMap<String, List<Item>>();

		for (final var header : headers)
		{
			final var path = header.path();
			if (path == null)
			{
				continue;
			}

			final var fileName = path.getFileName() == null ? null : path.getFileName().toString();
			final var qualifiedName = header.qualifiedName();
			final var item = new Item(path, qualifiedName);

			if (fileName != null && !fileName.isBlank())
			{
				byFileName.computeIfAbsent(fileName, k -> new ArrayList<>()).add(item);
			}

			if (qualifiedName != null && !qualifiedName.isBlank())
			{
				byQualifiedName.computeIfAbsent(qualifiedName, k -> new ArrayList<>()).add(item);
			}
		}

		final var duplicateFileNames = byFileName.entrySet()
												 .stream()
												 .filter(e -> e.getValue().size() > 1)
												 .sorted(Map.Entry.comparingByKey())
												 .map(e -> new DuplicateEntry(e.getKey(), sortItems(e.getValue())))
												 .toList();

		final var duplicateQualifiedNames = byQualifiedName.entrySet()
														   .stream()
														   .filter(e -> e.getValue().size() > 1)
														   .sorted(Map.Entry.comparingByKey())
														   .map(e -> new DuplicateEntry(e.getKey(), sortItems(e.getValue())))
														   .toList();

		return new Duplicates(duplicateFileNames, duplicateQualifiedNames);
	}

	private static List<Item> sortItems(final List<Item> items)
	{
		return items.stream()
					.sorted(Comparator.comparing(i -> i.path().toString()))
					.toList();
	}

	private static String safe(final String value)
	{
		return value == null ? "" : value;
	}

	private record Item(Path path, String qualifiedName)
	{
	}

	private record DuplicateEntry(String key, List<Item> items)
	{
	}

	private record Duplicates(List<DuplicateEntry> duplicateFileNames,
							  List<DuplicateEntry> duplicateQualifiedNames)
	{
	}
}

