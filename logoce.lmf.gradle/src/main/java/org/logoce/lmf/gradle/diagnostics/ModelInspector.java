package org.logoce.lmf.gradle.diagnostics;

import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.resource.parsing.ParseDiagnostic;
import org.logoce.lmf.model.resource.parsing.ParseDiagnostic.Severity;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class ModelInspector
{
	private ModelInspector()
	{
	}

	static List<ModelInspectionResult> inspect(final List<File> modelFiles)
	{
		final List<ModelInspectionResult> results = new ArrayList<>(modelFiles.size());
		for (final var file : modelFiles)
		{
			results.add(inspectSingle(file));
		}
		return results;
	}

	private static ModelInspectionResult inspectSingle(final File file)
	{
		try (final var inputStream = new FileInputStream(file))
		{
			final var parseResult = ResourceUtil.loadModelWithDiagnostics(inputStream, ModelRegistry.empty());
			final var roots = parseResult.roots();
			final var qualifiedName = extractQualifiedName(roots);
			final var imports = extractImports(roots);

			final var filteredDiagnostics = filterDiagnostics(parseResult.diagnostics());

			return new ModelInspectionResult(file,
											 qualifiedName,
											 imports,
											 filteredDiagnostics.isEmpty() ? List.of() : List.copyOf(filteredDiagnostics));
		}
		catch (IOException e)
		{
			return failureResult(file, e);
		}
		catch (Exception e)
		{
			return failureResult(file, e);
		}
	}

	private static Optional<String> extractQualifiedName(final List<Tree<PNode>> roots)
	{
		if (roots.isEmpty()) return Optional.empty();

		final var node = roots.getFirst().data();
		final var domain = extractValue(node, "domain");
		final var name = extractValue(node, "name");

		return name.map(n -> domain.map(d -> d + "." + n).orElse(n));
	}

	private static List<String> extractImports(final List<Tree<PNode>> roots)
	{
		if (roots.isEmpty()) return List.of();

		final var tokens = roots.getFirst().data().tokens().iterator();
		while (tokens.hasNext())
		{
			final var token = tokens.next();
			if (token.type() == ELMTokenType.VALUE_NAME && token.value().equals("imports"))
			{
				if (tokens.hasNext()) tokens.next(); // skip ASSIGN

				final List<String> imports = new ArrayList<>();
				while (tokens.hasNext())
				{
					final var next = tokens.next();
					if (next.type() == ELMTokenType.VALUE)
					{
						for (final var part : next.value().split(","))
						{
							final var trimmed = part.trim();
							if (trimmed.isEmpty() == false) imports.add(trimmed);
						}
					}
					else if (next.type() != ELMTokenType.LIST_SEPARATOR && next.type() != ELMTokenType.WHITE_SPACE)
					{
						break;
					}
				}
				return imports;
			}
		}
		return List.of();
	}

	private static Optional<String> extractValue(final PNode node, final String propertyName)
	{
		final var it = node.tokens().iterator();
		while (it.hasNext())
		{
			final var token = it.next();
			if (token.type() == ELMTokenType.VALUE_NAME && token.value().equals(propertyName))
			{
				if (it.hasNext()) it.next(); // skip ASSIGN
				if (it.hasNext())
				{
					return Optional.ofNullable(it.next().value());
				}
				break;
			}
		}
		return Optional.empty();
	}

	private static ModelInspectionResult failureResult(final File file, final Exception error)
	{
		final var message = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();

		final var contextual = contextualDiagnostics(file, message);
		if (contextual.isPresent())
		{
			return new ModelInspectionResult(file, Optional.empty(), List.of(), List.of(contextual.get()));
		}

		final var firstTokenDiag = firstTokenDiagnostic(file, message);
		if (firstTokenDiag.isPresent())
		{
			return new ModelInspectionResult(file, Optional.empty(), List.of(), List.of(firstTokenDiag.get()));
		}

		final var probe = ParserProbe.probe(file, error);
		if (probe.isPresent())
		{
			return new ModelInspectionResult(file, Optional.empty(), List.of(), List.of(probe.get()));
		}

		final var diagnostic = new ParseDiagnostic(1, 1, 1, 0, Severity.ERROR, message);
		return new ModelInspectionResult(file, Optional.empty(), List.of(), List.of(diagnostic));
	}

	private static List<ParseDiagnostic> filterDiagnostics(final List<ParseDiagnostic> diagnostics)
	{
		if (diagnostics.isEmpty()) return diagnostics;

		final List<ParseDiagnostic> filtered = new ArrayList<>(diagnostics.size());
		for (final var diagnostic : diagnostics)
		{
			final var message = diagnostic.message() == null ? "" : diagnostic.message();
			final boolean isImportResolution = message.contains("Cannot resolve model '") ||
											   message.contains("Cannot resolve imported model '");
			if (isImportResolution == false)
			{
				filtered.add(diagnostic);
			}
		}
		return filtered;
	}

	private static Optional<ParseDiagnostic> contextualDiagnostics(final File file, final String message)
	{
		try
		{
			final var source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			final var reader = new PTreeReader();
			final var diagnostics = new ArrayList<ParseDiagnostic>();
			final var result = reader.readWithDiagnostics(new FileInputStream(file), diagnostics);
			if (result.model().isEmpty() == false)
			{
				final var firstToken = result.model().getFirst().data().tokens().getFirst();
				final var span = spanOf(firstToken, source);
				return Optional.of(new ParseDiagnostic(span.line(), span.column(), Math.max(1, firstToken.length()),
													   firstToken.offset(), Severity.ERROR,
													   "Failed near '" + firstToken.value() + "': " + message));
			}
			if (diagnostics.isEmpty() == false)
			{
				return Optional.of(diagnostics.getFirst());
			}
		}
		catch (Exception ignored)
		{
		}
		return Optional.empty();
	}

	private static Optional<ParseDiagnostic> firstTokenDiagnostic(final File file, final String message)
	{
		try
		{
			final var source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			final var reader = new PTreeReader();
			final var roots = reader.read(new FileInputStream(file));
			if (roots.isEmpty()) return Optional.empty();

			final var firstToken = roots.getFirst().data().tokens().getFirst();
			final var span = spanOf(firstToken, source);
			return Optional.of(new ParseDiagnostic(span.line(), span.column(), Math.max(1, firstToken.length()),
												   firstToken.offset(), Severity.ERROR,
												   "Failed near '" + firstToken.value() + "': " + message));
		}
		catch (Exception ignored)
		{
			return Optional.empty();
		}
	}

	private static Span spanOf(final org.logoce.lmf.model.resource.parsing.PToken token, final CharSequence source)
	{
		final int offset = token.offset();
		int line = 1;
		int col = 1;
		for (int i = 0; i < offset && i < source.length(); i++)
		{
			if (source.charAt(i) == '\n')
			{
				line++;
				col = 1;
			}
			else
			{
				col++;
			}
		}
		return new Span(line, col);
	}

	private record Span(int line, int column)
	{}
}
