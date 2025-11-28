package org.logoce.lmf.editorfx.core;

import org.logoce.lmf.editorfx.diagnostic.Diagnostic;
import org.logoce.lmf.editorfx.diagnostic.DiagnosticSeverity;
import org.logoce.lmf.editorfx.symbol.Symbol;
import org.logoce.lmf.editorfx.symbol.SymbolExtractor;
import org.logoce.lmf.editorfx.semantic.SemanticModel;
import org.logoce.lmf.editorfx.semantic.SemanticModelBuilder;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.resource.ResourceUtil;
import org.logoce.lmf.model.resource.parsing.ParseDiagnostic;
import org.logoce.lmf.model.util.ModelRegistry;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class ModelService implements AutoCloseable {
	private final ScheduledExecutorService executor;
	private final Map<Document, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();
	private final Duration debounce;
	private final BiConsumer<Document, List<Diagnostic>> onParsed;
	private final BiConsumer<Document, Throwable> onError;
	private final Path workspaceRoot;
	private final DocumentManager documentManager;
	private final Map<Path, List<Symbol>> workspaceSymbols = new ConcurrentHashMap<>();

	public ModelService(Duration debounce, BiConsumer<Document, List<Diagnostic>> onParsed, BiConsumer<Document, Throwable> onError, Path workspaceRoot, DocumentManager documentManager) {
		this.debounce = debounce;
		this.onParsed = onParsed;
		this.onError = onError;
		this.workspaceRoot = workspaceRoot;
		this.documentManager = documentManager;
		this.executor = Executors.newSingleThreadScheduledExecutor(new DaemonFactory());
	}

	public void onDocumentChanged(Document document) {
		final ScheduledFuture<?> previous = scheduled.remove(document);
		if (previous != null) {
			previous.cancel(false);
		}
		final ScheduledFuture<?> future = executor.schedule(() -> {
			try {
				final List<Diagnostic> diagnostics = parse(document);
				onParsed.accept(document, diagnostics);
			} catch (Throwable t) {
				onError.accept(document, t);
			} finally {
				scheduled.remove(document);
			}
		}, debounce.toMillis(), TimeUnit.MILLISECONDS);
		scheduled.put(document, future);
	}

	private List<Diagnostic> parse(Document document) {
		final List<Diagnostic> diagnostics = new ArrayList<>();
		final var files = workspaceContents();
		final var registryContext = buildWorkspaceRegistry(files);
		final var registry = registryContext.registry();

		loadDocument(document, diagnostics, registry);
		parseWorkspace(document, diagnostics, files, registryContext);

		return diagnostics;
	}

	private void parseWorkspace(Document focusDoc, List<Diagnostic> diagnostics, List<FileContent> files, WorkspaceRegistry registryContext) {
		record Entry(Path path, ResourceUtil.ParseResult result) {}
		final List<Entry> results = new ArrayList<>();
		for (FileContent file : files) {
			try {
				try (var in = new ByteArrayInputStream(file.text().getBytes(StandardCharsets.UTF_8))) {
					results.add(new Entry(file.path(), ResourceUtil.loadModelWithDiagnostics(in, registryContext.registry())));
				}
			} catch (Exception e) {
				if (file.path().equals(focusDoc.path())) {
					final String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
					diagnostics.add(new Diagnostic(file.path(), 1, 1, 1, DiagnosticSeverity.ERROR, msg));
				}
			}
		}
		// propagate diagnostics affecting the focus document
		for (Entry entry : results) {
			if (!entry.path().equals(focusDoc.path())) continue;
			for (ParseDiagnostic d : entry.result.diagnostics()) {
				diagnostics.add(new Diagnostic(
					focusDoc.path(),
					d.line(),
					d.column(),
					Math.max(1, d.length()),
					switch (d.severity()) {
						case INFO -> DiagnosticSeverity.INFO;
						case WARNING -> DiagnosticSeverity.WARNING;
						case ERROR -> DiagnosticSeverity.ERROR;
					},
					d.message()
				));
			}
		}
		workspaceSymbols.clear();
		for (Entry entry : results) {
			workspaceSymbols.put(entry.path, SymbolExtractor.extract(entry.path, entry.result));
		}

		if (registryContext.linkError() != null) {
			diagnostics.add(new Diagnostic(focusDoc.path(), 1, 1, 1, DiagnosticSeverity.ERROR, registryContext.linkError()));
		}
	}

	public List<Symbol> allSymbols() {
		final var fromWorkspace = workspaceSymbols.values().stream().flatMap(Collection::stream);
		final var fromOpenDocs = documentManager.documents().stream().flatMap(doc -> doc.symbols().stream());
		final Map<String, Symbol> byKey = new ConcurrentHashMap<>();
		Stream.concat(fromWorkspace, fromOpenDocs).forEach(sym -> {
			final String key = sym.path().toString() + "#" + sym.name();
			byKey.putIfAbsent(key, sym);
		});
		return byKey.values().stream().toList();
	}

	private List<FileContent> workspaceContents() {
		try (var stream = Files.walk(workspaceRoot)) {
			return stream.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().endsWith(".lm"))
				.sorted()
				.map(path -> {
					final String text = documentManager.documents().stream()
						.filter(doc -> doc.path().equals(path))
						.map(Document::getText)
						.findFirst()
						.orElseGet(() -> {
							try {
								return Files.readString(path);
							} catch (IOException e) {
								return "";
							}
						});
					return new FileContent(path, text);
				})
				.toList();
		} catch (Exception e) {
			return List.of();
		}
	}

	private WorkspaceRegistry buildWorkspaceRegistry(List<FileContent> files) {
		// First try loading everything together (multi-model sort)
		final List<InputStream> allStreams = files.stream()
			.map(fc -> new ByteArrayInputStream(fc.text().getBytes(StandardCharsets.UTF_8)))
			.map(is -> (InputStream) is)
			.toList();
		try {
			final List<Model> models = ResourceUtil.loadModels(allStreams, ModelRegistry.empty());
			final var builder = new ModelRegistry.Builder(ModelRegistry.empty());
			for (Model model : models) {
				builder.register(model);
			}
			return new WorkspaceRegistry(builder.build(), null);
		} catch (Exception ignored) {
			// fall through to incremental loading
		}

		final var builder = new ModelRegistry.Builder(ModelRegistry.empty());
		ModelRegistry current = builder.build();
		final List<FileContent> remaining = new ArrayList<>(files);
		String linkError = null;

		boolean progress;
		do {
			progress = false;
			final var iterator = remaining.iterator();
			while (iterator.hasNext()) {
				final FileContent file = iterator.next();
				try (var in = new ByteArrayInputStream(file.text().getBytes(StandardCharsets.UTF_8))) {
					final var result = ResourceUtil.loadModelWithDiagnostics(in, current);
					if (result.model() != null) {
						builder.register(result.model());
						current = builder.build();
						iterator.remove();
						progress = true;
					} else if (linkError == null) {
						linkError = result.diagnostics().stream()
							.filter(d -> d.severity() == ParseDiagnostic.Severity.ERROR)
							.findFirst()
							.map(ParseDiagnostic::message)
							.orElse(null);
					}
				} catch (Exception e) {
					if (linkError == null) {
						linkError = e.getMessage() != null ? e.getMessage() : "Workspace link error";
					}
				}
			}
		} while (progress && !remaining.isEmpty());

		return new WorkspaceRegistry(current, remaining.isEmpty() ? null : linkError);
	}

	private void loadDocument(Document document, List<Diagnostic> diagnostics, ModelRegistry registry) {
		try (var in = new ByteArrayInputStream(document.getText().getBytes(StandardCharsets.UTF_8))) {
			final var result = ResourceUtil.loadModelWithDiagnostics(in, registry);
			for (ParseDiagnostic d : result.diagnostics()) {
				final int len = Math.max(1, d.length());
				diagnostics.add(new Diagnostic(
					document.path(),
					d.line(),
					d.column(),
					len,
					switch (d.severity()) {
						case INFO -> DiagnosticSeverity.INFO;
						case WARNING -> DiagnosticSeverity.WARNING;
						case ERROR -> DiagnosticSeverity.ERROR;
					},
					d.message()
				));
			}
			if (result.model() == null && diagnostics.isEmpty()) {
				diagnostics.add(new Diagnostic(document.path(), 1, 1, 1, DiagnosticSeverity.ERROR, "Failed to parse model"));
			}
			document.setSymbols(SymbolExtractor.extract(document.path(), result));
			document.setSemanticModel(SemanticModelBuilder.build(document.path(), result));
		} catch (Exception e) {
			final String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
			final int len = Math.min(document.getText().isEmpty() ? 1 : document.getText().length(), 5);
			diagnostics.add(new Diagnostic(document.path(), 1, 1, len, DiagnosticSeverity.ERROR, message));
			document.setSymbols(List.of());
			document.setSemanticModel(SemanticModel.empty());
		}
	}

	private record FileContent(Path path, String text) {}

	private record WorkspaceRegistry(ModelRegistry registry, String linkError) {}

	@Override
	public void close() {
		executor.shutdownNow();
	}

	private static final class DaemonFactory implements ThreadFactory {
		private int counter = 0;

		@Override
		public Thread newThread(Runnable r) {
			final Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("model-service-" + counter++);
			return t;
		}
	}
}
