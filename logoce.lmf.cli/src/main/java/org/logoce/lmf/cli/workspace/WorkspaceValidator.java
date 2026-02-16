package org.logoce.lmf.cli.workspace;

import org.logoce.lmf.cli.diagnostics.DiagnosticItem;
import org.logoce.lmf.cli.diagnostics.DiagnosticReporter;
import org.logoce.lmf.cli.diagnostics.ValidationReport;
import org.logoce.lmf.cli.util.PathDisplay;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.lang.Model;
import org.logoce.lmf.core.loader.api.loader.LmLoader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.loader.api.loader.model.LmDocument;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.loader.api.text.syntax.PNode;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class WorkspaceValidator
{
	public boolean validate(final RegistryService.PreparedWorkspace prepared,
							  final Path projectRoot,
							  final Map<Path, String> updatedSources,
							  final DocumentLoader documentLoader,
							  final PrintWriter err)
	{
		return validateWithReport(prepared, projectRoot, updatedSources, documentLoader, err).ok();
	}

	public ValidationReport validateWithReport(final RegistryService.PreparedWorkspace prepared,
										 final Path projectRoot,
										 final Map<Path, String> updatedSources,
										 final DocumentLoader documentLoader,
										 final PrintWriter err)
	{
		Objects.requireNonNull(prepared, "prepared");
		Objects.requireNonNull(projectRoot, "projectRoot");
		Objects.requireNonNull(updatedSources, "updatedSources");
		Objects.requireNonNull(documentLoader, "documentLoader");
		Objects.requireNonNull(err, "err");

		final var diagnostics = new ArrayList<DiagnosticItem>();
		final var messages = new ArrayList<String>();

		final var metaRegistry = rebuildMetaModelRegistryIfNeeded(prepared,
											 projectRoot,
											 updatedSources,
											 documentLoader,
											 diagnostics,
											 messages,
											 err);
		if (metaRegistry == null)
		{
			return new ValidationReport(false, List.copyOf(diagnostics), List.copyOf(messages));
		}

		final var registryBuilder = new ModelRegistry.Builder(metaRegistry);
		final var toLoad = new LinkedHashSet<String>();
		for (final var entry : prepared.registryModelPaths().entrySet())
		{
			final var name = entry.getKey();
			final var header = prepared.headersByQualifiedName().get(name);
			if (header == null || header.metaModelRoot())
			{
				continue;
			}
			toLoad.add(name);
		}

		final var remaining = new LinkedHashSet<String>(toLoad);
		final var loaded = new HashSet<String>();

		while (!remaining.isEmpty())
		{
			String next = null;
			for (final var candidate : remaining)
			{
				final var header = prepared.headersByQualifiedName().get(candidate);
				if (header == null)
				{
					continue;
				}

				final boolean depsLoaded = header.imports()
											  .stream()
											  .filter(remaining::contains)
											  .allMatch(loaded::contains);
				if (depsLoaded)
				{
					next = candidate;
					break;
				}
			}

			if (next == null)
			{
				next = remaining.iterator().next();
			}

			remaining.remove(next);
			final var path = prepared.registryModelPaths().get(next);
			final var source = resolveSource(updatedSources, documentLoader, path, err);
			if (source == null)
			{
				messages.add("Failed to read model source: " + PathDisplay.display(projectRoot, path));
				return new ValidationReport(false, List.copyOf(diagnostics), List.copyOf(messages));
			}

			final var doc = documentLoader.loadModelFromSource(registryBuilder.build(), next, source, err);
			if (doc == null)
			{
				messages.add("Failed to load model: " + PathDisplay.display(projectRoot, path));
				return new ValidationReport(false, List.copyOf(diagnostics), List.copyOf(messages));
			}
			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				final var displayPath = PathDisplay.display(projectRoot, path);
				DiagnosticReporter.printDiagnostics(err, displayPath, doc.diagnostics());
				for (final var diagnostic : doc.diagnostics())
				{
					diagnostics.add(new DiagnosticItem(displayPath, diagnostic));
				}
				return new ValidationReport(false, List.copyOf(diagnostics), List.copyOf(messages));
			}
			if (doc.model() instanceof Model model)
			{
				registryBuilder.remove(next);
				registryBuilder.register(model);
			}
			else
			{
				final var displayPath = PathDisplay.display(projectRoot, path);
				final var message = "Input doesn't define a valid model: " + displayPath;
				err.println(message);
				messages.add(message);
				return new ValidationReport(false, List.copyOf(diagnostics), List.copyOf(messages));
			}

			loaded.add(next);
		}

		return ValidationReport.success();
	}

	private static ModelRegistry rebuildMetaModelRegistryIfNeeded(final RegistryService.PreparedWorkspace prepared,
														  final Path projectRoot,
														  final Map<Path, String> updatedSources,
														  final DocumentLoader documentLoader,
														  final List<DiagnosticItem> diagnostics,
														  final List<String> messages,
														  final PrintWriter err)
	{
		final var targetHeader = prepared.targetHeader();
		final boolean needsMetaValidation = targetHeader != null
											  && targetHeader.metaModelRoot()
											  && updatedSources.containsKey(prepared.targetPath().toAbsolutePath().normalize());
		if (!needsMetaValidation)
		{
			return prepared.registry();
		}

		final var headerIndex = new org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex();
		try
		{
			headerIndex.refresh(projectRoot.toAbsolutePath().normalize());
		}
		catch (java.io.IOException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			final var fullMessage = "Failed to scan workspace: " + message;
			err.println(fullMessage);
			messages.add(fullMessage);
			return null;
		}

		final var closurePaths = resolveMetaModelClosure(prepared.targetQualifiedName(),
													  prepared.targetPath(),
													  projectRoot,
													  updatedSources,
													  documentLoader,
													  headerIndex,
													  diagnostics,
													  messages,
													  err);
		if (closurePaths == null)
		{
			return null;
		}

		final var metaModelDocuments = parseMetaModelDocuments(closurePaths,
													   projectRoot,
													   updatedSources,
													   documentLoader,
													   diagnostics,
													   messages,
													   err);
		if (metaModelDocuments == null)
		{
			return null;
		}

		final ModelRegistry registry;
		try
		{
			registry = LmLoader.buildRegistry(metaModelDocuments.stream().map(MetaModelDocument::document).toList(),
													  prepared.registry());
		}
		catch (RuntimeException e)
		{
			final var message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
			final var fullMessage = "Failed to load meta-models: " + message;
			err.println(fullMessage);
			messages.add(fullMessage);
			diagnostics.add(new DiagnosticItem(PathDisplay.display(projectRoot, prepared.targetPath()),
									   new LmDiagnostic(1,
														1,
														1,
														0,
														LmDiagnostic.Severity.ERROR,
														fullMessage)));
			return null;
		}

		for (final var metaModel : metaModelDocuments)
		{
			final var doc = documentLoader.loadModelFromSource(registry, metaModel.qualifiedName(), metaModel.source(), err);
			if (doc == null)
			{
				messages.add("Failed to load meta-model: " + PathDisplay.display(projectRoot, metaModel.path()));
				return null;
			}
			if (DiagnosticReporter.hasErrors(doc.diagnostics()))
			{
				DiagnosticReporter.printDiagnostics(err,
											 PathDisplay.display(projectRoot, metaModel.path()),
											 doc.diagnostics());
				final var displayPath = PathDisplay.display(projectRoot, metaModel.path());
				for (final var diagnostic : doc.diagnostics())
				{
					diagnostics.add(new DiagnosticItem(displayPath, diagnostic));
				}
				return null;
			}
			if (!(doc.model() instanceof Model))
			{
				final var fullMessage = "Input doesn't define a valid meta-model: " + PathDisplay.display(projectRoot, metaModel.path());
				err.println(fullMessage);
				messages.add(fullMessage);
				return null;
			}
		}

		return registry;
	}

	private static List<Path> resolveMetaModelClosure(final String targetQualifiedName,
													 final Path targetPath,
													 final Path projectRoot,
													 final Map<Path, String> updatedSources,
													 final DocumentLoader documentLoader,
													 final org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex headerIndex,
													 final List<DiagnosticItem> diagnostics,
													 final List<String> messages,
													 final PrintWriter err)
	{
		final var normalizedTarget = targetPath.toAbsolutePath().normalize();
		final var visited = new LinkedHashSet<String>();
		final var files = new LinkedHashSet<Path>();
		final var queue = new ArrayDeque<String>();

		queue.add(targetQualifiedName);
		while (!queue.isEmpty())
		{
			final var name = queue.removeFirst();
			if (name == null || name.isBlank() || !visited.add(name))
			{
				continue;
			}

			final Path path;
			final var candidates = headerIndex.headersByQualifiedName(name)
										 .stream()
										 .filter(org.logoce.lmf.core.loader.api.tooling.workspace.DiskModelHeaderIndex.DiskModelHeader::metaModelRoot)
										 .toList();

			if (candidates.isEmpty() && name.equals(targetQualifiedName))
			{
				path = normalizedTarget;
			}
			else if (candidates.isEmpty())
			{
				final var fullMessage = "Meta-model not found: " + name;
				err.println(fullMessage);
				messages.add(fullMessage);
				return null;
			}
			else if (candidates.size() > 1)
			{
				final var fullMessage = "Duplicate meta-model qualified name: " + name;
				err.println(fullMessage);
				messages.add(fullMessage);
				for (final var candidate : candidates)
				{
					final var detail = " - " + candidate.path();
					err.println(detail);
					messages.add(detail);
				}
				return null;
			}
			else
			{
				path = candidates.getFirst().path().toAbsolutePath().normalize();
			}

			final var source = resolveSource(updatedSources, documentLoader, path, err);
			if (source == null)
			{
				messages.add("Failed to read meta-model source: " + PathDisplay.display(projectRoot, path));
				return null;
			}

			final var imports = parseMetaModelImports(source, diagnostics, messages, err);
			if (imports == null)
			{
				return null;
			}

			files.add(path);
			for (final var imp : imports)
			{
				if (imp != null && !imp.isBlank())
				{
					queue.add(imp);
				}
			}
		}

		return List.copyOf(files);
	}

	private record MetaModelDocument(Path path, String qualifiedName, LmDocument document, String source)
	{
	}

	private static List<MetaModelDocument> parseMetaModelDocuments(final List<Path> closurePaths,
														 final Path projectRoot,
														 final Map<Path, String> updatedSources,
														 final DocumentLoader documentLoader,
														 final List<DiagnosticItem> diagnosticsOut,
														 final List<String> messagesOut,
														 final PrintWriter err)
	{
		final var reader = new LmTreeReader();
		final var documents = new ArrayList<MetaModelDocument>(closurePaths.size());

		for (final var path : closurePaths)
		{
			final var normalizedPath = path.toAbsolutePath().normalize();
			final var displayPath = PathDisplay.display(projectRoot, normalizedPath);
			final var source = resolveSource(updatedSources, documentLoader, normalizedPath, err);
			if (source == null)
			{
				messagesOut.add("Failed to read meta-model source: " + displayPath);
				return null;
			}

			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var readResult = reader.read(source, diagnostics);

			if (DiagnosticReporter.hasErrors(diagnostics))
			{
				DiagnosticReporter.printDiagnostics(err, displayPath, diagnostics);
				for (final var diagnostic : diagnostics)
				{
					diagnosticsOut.add(new DiagnosticItem(displayPath, diagnostic));
				}
				return null;
			}

			final var roots = readResult.roots();
			if (roots.isEmpty() || !ModelHeaderUtil.isMetaModelRoot(roots))
			{
				final var message = "Input doesn't define a valid meta-model: " + displayPath;
				err.println(message);
				messagesOut.add(message);
				return null;
			}

			final Tree<PNode> root = roots.getFirst();
			final var rootNode = root.data();
			final var name = ModelHeaderUtil.resolveName(rootNode);
			if (name == null || name.isBlank())
			{
				final var message = "Cannot resolve meta-model name for " + displayPath;
				err.println(message);
				messagesOut.add(message);
				return null;
			}
			final var domain = ModelHeaderUtil.resolveDomain(rootNode);
			final var qualifiedName = domain == null || domain.isBlank()
											  ? name
											  : domain + "." + name;

			final var document = new LmDocument(null,
									List.copyOf(diagnostics),
									List.copyOf(roots),
									readResult.source(),
									List.of());
			documents.add(new MetaModelDocument(normalizedPath, qualifiedName, document, source));
		}

		return List.copyOf(documents);
	}

	private static List<String> parseMetaModelImports(final String source,
												  final List<DiagnosticItem> diagnosticsOut,
												  final List<String> messagesOut,
												  final PrintWriter err)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var reader = new LmTreeReader();
		final var readResult = reader.read(source, diagnostics);
		if (DiagnosticReporter.hasErrors(diagnostics))
		{
			DiagnosticReporter.printDiagnostics(err, "<model>", diagnostics);
			for (final var diagnostic : diagnostics)
			{
				diagnosticsOut.add(new DiagnosticItem("<model>", diagnostic));
			}
			messagesOut.add("Cannot parse meta-model header");
			return null;
		}
		if (readResult.roots().isEmpty() || !ModelHeaderUtil.isMetaModelRoot(readResult.roots()))
		{
			return List.of();
		}

		final Tree<PNode> root = readResult.roots().getFirst();
		return List.copyOf(ModelHeaderUtil.resolveImports(root.data()));
	}

	private static String resolveSource(final Map<Path, String> updatedSources,
										final DocumentLoader documentLoader,
										final Path path,
										final PrintWriter err)
	{
		final var updated = updatedSources.get(path);
		if (updated != null)
		{
			return updated;
		}
		return documentLoader.readString(path, err);
	}
}
