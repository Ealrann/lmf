package org.logoce.lmf.cli.json;

import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.util.TextPositions;

import java.util.Objects;

public final class JsonSerializers
{
	private JsonSerializers()
	{
	}

	public static void writeDiagnostic(final JsonWriter json, final LmDiagnostic diagnostic)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(diagnostic, "diagnostic");

		json.beginObject()
			.name("line").value(diagnostic.line())
			.name("column").value(diagnostic.column())
			.name("length").value(diagnostic.length())
			.name("offset").value(diagnostic.offset())
			.name("severity").value(diagnostic.severity().name())
			.name("message").value(diagnostic.message())
			.endObject();
	}

	public static void writeLocatedDiagnostic(final JsonWriter json, final String file, final LmDiagnostic diagnostic)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(file, "file");
		Objects.requireNonNull(diagnostic, "diagnostic");

		json.beginObject()
			.name("file").value(file)
			.name("line").value(diagnostic.line())
			.name("column").value(diagnostic.column())
			.name("length").value(diagnostic.length())
			.name("offset").value(diagnostic.offset())
			.name("severity").value(diagnostic.severity().name())
			.name("message").value(diagnostic.message())
			.endObject();
	}

	public static void writeSpan(final JsonWriter json, final TextPositions.Span span)
	{
		Objects.requireNonNull(json, "json");
		Objects.requireNonNull(span, "span");

		json.beginObject()
			.name("line").value(span.line())
			.name("column").value(span.column())
			.name("length").value(span.length())
			.name("offset").value(span.offset())
			.endObject();
	}
}
