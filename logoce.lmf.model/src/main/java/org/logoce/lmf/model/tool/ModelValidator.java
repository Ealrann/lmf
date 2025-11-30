package org.logoce.lmf.model.tool;

import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.resource.parsing.ParseDiagnostic;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple CLI entry point to validate a .lm model with optional imports.
 *
 * Usage:
 *   ModelValidator <modelToValidate> <comma-separated-imports>
 *
 * Only diagnostics for the main model are printed unless imported models fail to load,
 * in which case their errors are reported to explain why validation could not proceed.
 */
public final class ModelValidator
{
	public static void main(final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			System.err.println("Usage: ModelValidator <model> [imports comma-separated]");
			System.exit(1);
		}

		final var targetFile = new File(args[0]);
		final var importFiles = args.length >= 2 && args[1] != null && !args[1].isBlank()
								? Arrays.stream(args[1].split(","))
										.map(String::trim)
										.filter(s -> !s.isEmpty())
										.map(File::new)
										.toList()
								: List.<File>of();

		final var validator = new ModelValidator();
		final var exit = validator.validate(targetFile, importFiles);
		System.exit(exit);
	}

	public int validate(final File target, final List<File> imports)
	{
		if (!target.isFile())
		{
			System.err.println("Model file not found: " + target);
			return 1;
		}

		final Map<File, ValidationResult> parsed = new HashMap<>();
		final var registry = new ModelRegistry.Builder(ModelRegistry.empty());

		// load imports first; collect their diagnostics but continue if they succeed
		for (final var file : imports)
		{
			final var result = loadWithDiagnostics(file, registry.build());
			parsed.put(file, result);
			if (result.model() != null)
			{
				registry.register(result.model());
			}
		}

		final var targetResult = loadWithDiagnostics(target, registry.build());
		parsed.put(target, targetResult);

		final var importFailures = imports.stream()
										  .map(parsed::get)
										  .filter(r -> r == null || hasErrors(r.diagnostics()))
										  .toList();
		if (!importFailures.isEmpty())
		{
			System.err.println("Validation failed: imported models have errors or could not be loaded.");
			for (final var file : imports)
			{
				final var res = parsed.get(file);
				if (res == null)
				{
					System.err.println(" - missing parse result for import: " + file.getPath());
					continue;
				}
				if (hasErrors(res.diagnostics()) == false) continue;
				System.err.println("Import: " + file.getPath());
				res.diagnostics().forEach(diag -> printDiagnostic(file.getPath(), diag));
			}
			return 1;
		}

		final var errors = targetResult.diagnostics();
		if (errors.isEmpty())
		{
			System.out.println("OK: " + target.getPath());
			return 0;
		}
		System.err.println("Errors in " + target.getPath() + ":");
		errors.forEach(diag -> printDiagnostic(target.getPath(), diag));
		return hasErrors(errors) ? 1 : 0;
	}

	private ValidationResult loadWithDiagnostics(final File file, final ModelRegistry registry)
	{
		try (final var inputStream = Files.newInputStream(file.toPath()))
		{
			final var loader = new LmLoader(registry);
			final var document = loader.loadModel(inputStream);

			final Model model = document.model();
			final List<ParseDiagnostic> diagnostics = document.diagnostics()
															  .stream()
															  .map(ModelValidator::toParseDiagnostic)
															  .toList();
			return new ValidationResult(model, diagnostics);
		}
		catch (IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			final var diag = new ParseDiagnostic(1,
												 1,
												 1,
												 0,
												 ParseDiagnostic.Severity.ERROR,
												 "Failed to load " + file.getPath() + ": " + message);
			return new ValidationResult(null, List.of(diag));
		}
	}

	private static boolean hasErrors(final List<ParseDiagnostic> diagnostics)
	{
		return diagnostics.stream().anyMatch(d -> d.severity() == ParseDiagnostic.Severity.ERROR);
	}

	private static void printDiagnostic(final String source, final ParseDiagnostic diag)
	{
		System.err.printf("%s:%d:%d [%s] %s%n",
						  source,
						  diag.line(),
						  diag.column(),
						  diag.severity(),
						  diag.message());
	}

	private static ParseDiagnostic toParseDiagnostic(final LmDiagnostic diagnostic)
	{
		final ParseDiagnostic.Severity severity = switch (diagnostic.severity())
		{
			case INFO -> ParseDiagnostic.Severity.INFO;
			case WARNING -> ParseDiagnostic.Severity.WARNING;
			case ERROR -> ParseDiagnostic.Severity.ERROR;
		};

		return new ParseDiagnostic(diagnostic.line(),
								   diagnostic.column(),
								   diagnostic.length(),
								   diagnostic.offset(),
								   severity,
								   diagnostic.message());
	}

	private record ValidationResult(Model model, List<ParseDiagnostic> diagnostics)
	{
	}
}
