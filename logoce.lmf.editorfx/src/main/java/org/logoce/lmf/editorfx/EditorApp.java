package org.logoce.lmf.editorfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.logoce.lmf.editorfx.core.Document;
import org.logoce.lmf.editorfx.core.DocumentManager;
import org.logoce.lmf.editorfx.core.ModelService;
import org.logoce.lmf.editorfx.diagnostic.Diagnostic;
import org.logoce.lmf.editorfx.lang.SyntaxHighlighter;
import org.logoce.lmf.editorfx.nav.NavigationHistory;
import org.logoce.lmf.editorfx.nav.NavigationService;
import org.logoce.lmf.editorfx.semantic.NameResolver;
import org.logoce.lmf.editorfx.symbol.Symbol;
import org.logoce.lmf.editorfx.symbol.SymbolPane;
import org.logoce.lmf.editorfx.symbol.Reference;
import org.logoce.lmf.editorfx.symbol.ReferenceFinder;
import org.logoce.lmf.editorfx.ui.*;
import org.logoce.lmf.editorfx.ui.ReferenceHighlighter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EditorApp extends Application {
	private Path workspaceRoot;
	private WorkspaceBrowser workspaceBrowser;
	private TabPane tabPane;
	private TextArea logArea;
	private Label workspaceLabel;
	private HBox findBar;
	private TextField findField;
	private SymbolPane symbolPane;
	private ReferencesPane referencesPane;
	private ErrorPane errorPane;
	private DocumentManager documentManager;
	private ModelService modelService;
	private SyntaxHighlighter highlighter;
	private NavigationService navigationService;
	private ReferenceHighlighter referenceHighlighter;
	private final NameResolver nameResolver = new NameResolver();
	private static final List<String> KEYWORD_COMPLETIONS = List.of(
		"MetaModel", "Group", "Definition", "Enum", "Unit",
		"+att", "-att", "+contains", "-contains", "+refers", "-refers", "includes", "Generic", "generics", "reference", "parameters"
	);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		workspaceRoot = resolveWorkspace(getParameters().getRaw());
		documentManager = new DocumentManager();
		modelService = new ModelService(
			Duration.ofMillis(400),
			(doc, diagnostics) -> Platform.runLater(() -> handleDiagnostics(doc, diagnostics)),
			(doc, err) -> Platform.runLater(() -> handleParseError(doc, err)),
			workspaceRoot,
			documentManager
		);
		highlighter = new SyntaxHighlighter();

		workspaceBrowser = new WorkspaceBrowser();
		workspaceBrowser.setOnOpen(this::openFile);
		navigationService = new NavigationService(new NavigationHistory(), this::currentState, this::openNavEntry);
		referenceHighlighter = new ReferenceHighlighter(highlighter);

		tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
			refreshSymbolsView(currentState());
			final EditorTabState state = currentState();
			if (state != null) {
				selectFileInList(state.document().path());
				updateReferencesAtCaret(state);
			} else {
				referencesPane.setReferences(List.of());
			}
		});

		buildFindBar();

		symbolPane = new SymbolPane();
		symbolPane.setOnJump(this::jumpToSymbol);
		referencesPane = new ReferencesPane();
		referencesPane.setOnJump(this::jumpToReference);
		errorPane = new ErrorPane();

		logArea = new TextArea();
		logArea.setEditable(false);
		logArea.setPrefRowCount(4);
		logArea.setWrapText(false);
		logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11;");
		logArea.getStyleClass().add("log-area");

		workspaceLabel = new Label(workspaceRoot.toString());

		final var root = new BorderPane();
		root.setTop(buildToolbar());
		root.setLeft(workspaceBrowser.node());
		root.setCenter(buildCenterPane());
		root.setRight(symbolPane.buildNode());
		root.setBottom(buildBottomPane());

		final var scene = new Scene(root, 1400, 900);
		final var css = EditorApp.class.getResource("/org/logoce/lmf/editorfx/editor.css");
		if (css != null) {
			scene.getStylesheets().add(css.toExternalForm());
		}
		installShortcuts(scene);
		stage.setTitle("LMF EditorFX");
		stage.setScene(scene);
		stage.show();

		refreshFileList();
	}

	@Override
	public void stop() {
		if (modelService != null) {
			modelService.close();
		}
	}

	private ToolBar buildToolbar() {
		final var refresh = new Button("Refresh");
		refresh.setOnAction(event -> refreshFileList());

		final var newFile = new Button("New .lm");
		newFile.setOnAction(event -> createNewFile());

		final var save = new Button("Save");
		save.setOnAction(event -> saveCurrentTab());

		final var saveAll = new Button("Save all");
		saveAll.setOnAction(event -> saveAllTabs());

		final var spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		final var bar = new ToolBar(
			new Label("Workspace: "),
			workspaceLabel,
			new Separator(Orientation.VERTICAL),
			refresh,
			newFile,
			save,
			saveAll,
			spacer
		);
		bar.setPadding(new Insets(6, 8, 6, 8));
		return bar;
	}

	private Node buildLogView() {
		final var title = new Label("Log");
		title.setStyle("-fx-font-weight: bold;");
		final var header = new HBox(title);
		header.setPadding(new Insets(4, 6, 4, 6));
		header.getStyleClass().add("pane-background");

		final var container = new BorderPane();
		container.setTop(header);
		container.setCenter(logArea);
		container.setPadding(new Insets(6, 6, 6, 6));
		container.setMinHeight(120);
		container.getStyleClass().add("pane-background");
		return container;
	}

	private Node buildBottomPane() {
		final var split = new SplitPane();
		split.setDividerPositions(1.0 / 3.0, 2.0 / 3.0);
		split.getItems().addAll(referencesPane.buildNode(), errorPane.node(), buildLogView());
		split.setPrefHeight(200);
		return split;
	}

	private Node buildCenterPane() {
		final var box = new VBox();
		box.getChildren().addAll(findBar, tabPane);
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		return box;
	}

	private void buildFindBar() {
		findField = new TextField();
		findField.setPromptText("Find...");
		findField.setOnAction(event -> performFindNext());

		final var findNext = new Button("Find next");
		findNext.setOnAction(event -> performFindNext());

		final var close = new Button("Close");
		close.setOnAction(event -> hideFindBar());

		findBar = new HBox(6, new Label("Find:"), findField, findNext, close);
		findBar.setPadding(new Insets(4, 6, 4, 6));
		findBar.setVisible(false);
		findBar.managedProperty().bind(findBar.visibleProperty());
	}

	private void refreshFileList() {
		workspaceBrowser.refresh(workspaceRoot, documentManager.documents(), this::log);
	}

	private void openFile(Path file) {
		for (var tab : tabPane.getTabs()) {
			final Object data = tab.getUserData();
			if (data instanceof EditorTabState state && Objects.equals(state.document().path(), file)) {
				tabPane.getSelectionModel().select(tab);
				return;
			}
		}

		final Document document;
		try {
			document = documentManager.load(file, modelService::onDocumentChanged);
		} catch (IOException e) {
			log("Failed to open " + file + ": " + e.getMessage());
			return;
		}

		final var area = new CodeArea();
		area.replaceText(document.getText());
		area.setWrapText(false);
		area.setStyle("-fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 12; -fx-caret-color: white;");
		final var tab = new Tab(file.getFileName().toString(), area);
		final var state = new EditorTabState(document, area);
		area.setOnMouseClicked(event -> {
			if (event.isControlDown() && event.getClickCount() == 1) {
				final var hit = area.hit(event.getX(), event.getY());
				final int caret = hit.getInsertionIndex();
				area.moveTo(caret);
				gotoDefinition(state, caret);
				event.consume();
			}
		});
		tab.setUserData(state);
		tab.setOnClosed(event -> log("Closed " + file));
		area.textProperty().addListener((obs, oldText, newText) -> {
			document.setText(newText);
			applyHighlight(area, newText, state);
			updateTabLabel(tab, state);
			updateFileListDirty(document);
		});
		area.caretPositionProperty().addListener((obs, oldPos, newPos) -> updateReferencesAtCaret(state));
		document.setChangeListener(doc -> {
			modelService.onDocumentChanged(doc);
			updateTabLabel(tab, state);
		});
		modelService.onDocumentChanged(document);
		applyHighlight(area, document.getText(), state);
		updateTabLabel(tab, state);
		tabPane.getTabs().add(tab);
		tabPane.getSelectionModel().select(tab);
		selectFileInList(file);
		refreshSymbolsView(state);
		log("Opened " + file);
	}

	private void saveCurrentTab() {
		final Tab tab = tabPane.getSelectionModel().getSelectedItem();
		if (tab == null) {
			log("Nothing to save");
			return;
		}
		final Object data = tab.getUserData();
		if (!(data instanceof EditorTabState state)) {
			log("Nothing to save");
			return;
		}
		try {
			documentManager.save(state.document());
			updateTabLabel(tab, state);
			updateFileListDirty(state.document());
			log("Saved " + state.document().path());
		} catch (IOException e) {
			log("Failed to save " + state.document().path() + ": " + e.getMessage());
		}
	}

	private void saveAllTabs() {
		for (var tab : tabPane.getTabs()) {
			final Object data = tab.getUserData();
			if (!(data instanceof EditorTabState state)) {
				continue;
			}
			try {
				documentManager.save(state.document());
				updateTabLabel(tab, state);
				updateFileListDirty(state.document());
			} catch (IOException e) {
				log("Failed to save " + state.document().path() + ": " + e.getMessage());
			}
		}
		log("Save all completed");
	}

	private void findInCurrentTab() {
		if (!findBar.isVisible()) {
			showFindBar();
		} else {
			performFindNext();
		}
	}

	private void createNewFile() {
		final var chooser = new DirectoryChooser();
		chooser.setTitle("Choose folder for .lm file");
		chooser.setInitialDirectory(workspaceRoot.toFile());
		final var folder = chooser.showDialog(tabPane.getScene().getWindow());
		if (folder == null) {
			return;
		}

		final var nameDialog = new TextInputDialog("model.lm");
		nameDialog.setTitle("New .lm file");
		nameDialog.setHeaderText("Enter file name");
		final Optional<String> result = nameDialog.showAndWait();
		if (result.isEmpty()) {
			return;
		}
		final String rawName = result.get().strip();
		if (rawName.isEmpty()) {
			log("File name cannot be empty");
			return;
		}
		final String fileName = rawName.endsWith(".lm") ? rawName : rawName + ".lm";
		final Path target = folder.toPath().resolve(fileName);
		try {
			if (!Files.exists(target)) {
				Files.createDirectories(target.getParent());
				Files.writeString(target, "(MetaModel domain=your.domain name=Model)\n", StandardCharsets.UTF_8);
				log("Created " + target);
			} else {
				log(target + " already exists; opening existing file");
			}
			refreshFileList();
			openFile(target);
		} catch (IOException e) {
			log("Failed to create " + target + ": " + e.getMessage());
		}
	}

	private Path resolveWorkspace(List<String> args) {
		final Path candidate = args.isEmpty() ? Path.of("").toAbsolutePath() : Path.of(args.getFirst()).toAbsolutePath();
		return Files.isDirectory(candidate) ? candidate : Path.of("").toAbsolutePath();
	}

	private void log(String message) {
		final String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		if (logArea != null) {
			logArea.appendText("[" + timestamp + "] " + message + System.lineSeparator());
		} else {
			System.out.println("[" + timestamp + "] " + message);
		}
	}

	private void handleDiagnostics(Document doc, List<Diagnostic> diagnostics) {
		doc.setDiagnostics(diagnostics);
		if (errorPane != null) {
			errorPane.setDiagnostics(flattenDiagnostics());
		}
		for (var tab : tabPane.getTabs()) {
			if (tab.getUserData() instanceof EditorTabState state && state.document().path().equals(doc.path())) {
				refreshGutter(state);
				applyHighlight(state.area(), doc.getText(), state);
				log("Symbols for " + doc.path().getFileName() + ": " + state.document().symbols().stream().map(Symbol::name).toList());
				break;
			}
		}
		if (diagnostics.isEmpty()) {
			log("Parsed " + doc.path().getFileName() + " (ok)");
		} else {
			for (Diagnostic diag : diagnostics) {
				log("[" + diag.severity() + "] " + doc.path().getFileName() + ":" + diag.line() + ":" + diag.column() + " " + diag.message());
			}
		}
		if (!tabPane.getTabs().isEmpty()) {
			refreshSymbolsView(currentState());
		}
	}

	private void installShortcuts(Scene scene) {
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), this::saveCurrentTab);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), this::saveAllTabs);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), this::findInCurrentTab);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN), this::showCompletion);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), () -> findReferences(true));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), this::renameSymbol);
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			if (event.getButton() == MouseButton.BACK) {
				navigationService.navigateBack();
				event.consume();
			} else if (event.getButton() == MouseButton.FORWARD) {
				navigationService.navigateForward();
				event.consume();
			}
		});
	}

	private void updateTabLabel(Tab tab, EditorTabState state) {
		final String name = state.document().path().getFileName().toString();
		final var nameLabel = new Label(name);
		final var starLabel = new Label(state.document().isDirty() ? " *" : "");
		starLabel.setStyle("-fx-font-weight: bold;");
		final var box = new HBox(2, nameLabel, starLabel);
		tab.setText(null);
		tab.setGraphic(box);
	}

	private void applyHighlight(CodeArea area, String text, EditorTabState state) {
		referenceHighlighter.applyHighlight(area, state.document());
	}

	private void showFindBar() {
		findBar.setVisible(true);
		findField.requestFocus();
		findField.selectAll();
	}

	private void hideFindBar() {
		findBar.setVisible(false);
	}

	private void performFindNext() {
		final Tab tab = tabPane.getSelectionModel().getSelectedItem();
		if (tab == null || !(tab.getUserData() instanceof EditorTabState state)) {
			log("Nothing to search");
			return;
		}
		final String needle = findField.getText();
		if (needle == null || needle.isEmpty()) {
			log("Enter text to find");
			showFindBar();
			return;
		}
		final String haystack = state.area().getText();
		if (haystack.isEmpty()) {
			log("File is empty");
			return;
		}
		final int start = Math.max(state.area().getSelection().getEnd(), state.area().getCaretPosition());
		int idx = haystack.indexOf(needle, start);
		if (idx < 0) {
			idx = haystack.indexOf(needle);
		}
		if (idx >= 0) {
			state.area().selectRange(idx, idx + needle.length());
			state.area().requestFocus();
			log("Found \"" + needle + "\"");
		} else {
			log("Not found: \"" + needle + "\"");
		}
	}

	private sealed interface BrowserRow permits FolderRow, FileRow {
	}

	private static final class FolderRow implements BrowserRow {
		private final Path folder;
		private final String label;

		FolderRow(Path folder, String label) {
			this.folder = folder;
			this.label = label;
		}

		Path folder() {
			return folder;
		}

		String label() {
			return label;
		}
	}

	private static final class FileRow implements BrowserRow {
		private final Path folder;
		private final Path path;
		private boolean modified;

		FileRow(Path folder, Path path) {
			this.folder = folder;
			this.path = path;
		}

		Path folder() {
			return folder;
		}

		Path path() {
			return path;
		}

		boolean modified() {
			return modified;
		}

		void setModified(boolean modified) {
			this.modified = modified;
		}
	}

	public static record EditorTabState(Document document, CodeArea area) {
	}
	
	private void refreshGutter(EditorTabState state) {
		state.area().setParagraphGraphicFactory(index -> {
			final int lineNumber = index + 1;
			final var onLine = state.document().diagnostics().stream()
				.filter(d -> d.line() == lineNumber)
				.toList();
			final var label = new Label(String.valueOf(lineNumber));
			label.getStyleClass().add("line-number");
			if (!onLine.isEmpty()) {
				final boolean hasError = onLine.stream().anyMatch(d -> d.severity() == org.logoce.lmf.editorfx.diagnostic.DiagnosticSeverity.ERROR);
				label.getStyleClass().add(hasError ? "line-error" : "line-warning");
				label.setText(hasError ? "!" : "•");
			}
			return label;
		});
	}

	private void refreshSymbolsView(EditorTabState state) {
		if (state == null) {
			symbolPane.setSymbols(List.of());
			return;
		}
		symbolPane.setSymbols(state.document().symbols());
		updateFileListDirty(state.document());
	}

	private void selectFileInList(Path path) {
		workspaceBrowser.select(path);
	}

	private EditorTabState currentState() {
		final var tab = tabPane.getSelectionModel().getSelectedItem();
		if (tab == null) return null;
		final Object data = tab.getUserData();
		return data instanceof EditorTabState state ? state : null;
	}

	private void jumpToSymbol(Symbol symbol) {
		if (symbol == null) return;
		final var state = findState(symbol.path());
		if (state == null) return;
		final int caret = Math.min(state.area().getLength(), symbol.offset());
		state.area().moveTo(caret);
		state.area().requestFollowCaret();
	}

	private EditorTabState findState(Path path) {
		for (var tab : tabPane.getTabs()) {
			if (tab.getUserData() instanceof EditorTabState st && st.document().path().equals(path)) {
				tabPane.getSelectionModel().select(tab);
				return st;
			}
		}
		return null;
	}

	private void jumpToReference(Reference ref) {
		if (ref == null) return;
		recordLocation(currentState(), true);
		if (!openOrSelect(ref.path())) {
			return;
		}
		final var state = currentState();
		if (state == null) return;
		final int caret = Math.min(state.area().getLength(), ref.offset());
		state.area().selectRange(caret, caret + ref.length());
		state.area().requestFollowCaret();
	}

	private boolean openOrSelect(Path path) {
		if (path == null) return false;
		for (var tab : tabPane.getTabs()) {
			if (tab.getUserData() instanceof EditorTabState st && st.document().path().equals(path)) {
				tabPane.getSelectionModel().select(tab);
				return true;
			}
		}
		openFile(path);
		return currentState() != null && currentState().document().path().equals(path);
	}

	private void openNavEntry(Path path, int caret) {
		if (path == null) return;
		openFile(path);
		final var state = findState(path);
		if (state == null) return;
		final int pos = Math.min(state.area().getLength(), Math.max(0, caret));
		state.area().moveTo(pos);
		state.area().requestFollowCaret();
	}

	private void gotoDefinition(EditorTabState state, int caret) {
		recordLocation(state, true);
		final String text = state.area().getText();
		final List<String> candidates = new ArrayList<>();
		final String token = referenceToken(text, caret);
		candidates.addAll(referenceNames(text, caret));
		final String word = TextOps.wordAt(text, caret);
		if (word != null && !word.isEmpty()) {
			candidates.add(word);
		}
		final Symbol target = nameResolver.resolve(
			state.document(),
			caret,
			token,
			candidates,
			modelService.allSymbols()
		);
		if (target != null) {
			if (!state.document().path().equals(target.path())) {
				openFile(target.path());
				final var newState = findState(target.path());
				if (newState != null) {
					jumpToSymbol(target);
				}
			} else {
				jumpToSymbol(target);
			}
		} else {
			log("No definition found for '" + word + "'");
		}
	}

	private Symbol findSymbolByNames(EditorTabState state, List<String> names) {
		Symbol target = null;
		for (String name : names) {
			target = state.document().symbols().stream()
				.filter(sym -> sym.name().equals(name))
				.findFirst()
				.orElse(null);
			if (target == null) {
				target = modelService.allSymbols().stream()
					.filter(sym -> sym.name().equals(name))
					.findFirst()
					.orElse(null);
			}
			if (target != null) break;
		}
		return target;
	}

	private Symbol findSymbolByNames(EditorTabState state, List<String> names, int rangeStart, int rangeEnd) {
		for (String name : names) {
			final var local = state.document().symbols().stream()
				.filter(sym -> sym.name().equals(name))
				.filter(sym -> sym.offset() >= rangeStart && (rangeEnd < 0 || sym.offset() < rangeEnd))
				.min(Comparator.comparingInt(Symbol::offset));
			if (local.isPresent()) return local.get();
		}
		return null;
	}

	private Symbol symbolAtCaret(EditorTabState state, int caret, int rangeStart, int rangeEnd) {
		return state.document().symbols().stream()
			.filter(sym -> sym.offset() >= rangeStart && (rangeEnd < 0 || sym.offset() < rangeEnd))
			.filter(sym -> caret >= sym.offset() && caret <= sym.offset() + sym.length())
			.min(Comparator.comparingInt(Symbol::offset))
			.orElse(null);
	}

	private Reference symbolToReference(Symbol symbol) {
		return new Reference(symbol.name(), symbol.path(), symbol.offset(), symbol.length(), symbol.line(), symbol.column());
	}

	private Symbol resolveGenericsIndex(EditorTabState state, String token, int caret) {
		if (token == null) return null;
		final var matcher = java.util.regex.Pattern.compile("generics\\.(\\d+)").matcher(token);
		if (!matcher.find()) return null;
		final int idx = Integer.parseInt(matcher.group(1));
		final var symbols = state.document().symbols();
		final int containerStart = nearestContainerStart(symbols, caret);
		final int containerEnd = nextContainerStart(symbols, caret, containerStart);
		final var generics = state.document().symbols().stream()
			.filter(sym -> sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.GENERIC)
			.filter(sym -> sym.offset() >= containerStart && (containerEnd < 0 || sym.offset() < containerEnd))
			.sorted(Comparator.comparingInt(Symbol::offset))
			.toList();
		if (idx >= 0) {
			if (idx < generics.size()) return generics.get(idx);
		}
		return null;
	}

	private void handleParseError(Document doc, Throwable err) {
		final String message = err != null && err.getMessage() != null ? err.getMessage() : "Parse error";
		log("Parse failed: " + message);
		if (doc != null) {
			final var diag = new Diagnostic(doc.path(), 1, 1, 1, org.logoce.lmf.editorfx.diagnostic.DiagnosticSeverity.ERROR, message);
			handleDiagnostics(doc, List.of(diag));
		}
	}

	private List<Diagnostic> flattenDiagnostics() {
		return documentManager.documents().stream()
			.flatMap(d -> d.diagnostics().stream())
			.toList();
	}

	private int nearestContainerStart(List<Symbol> symbols, int caret) {
		return symbols.stream()
			.filter(sym -> sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.DEFINITION
				|| sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.GROUP
				|| sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.METAMODEL)
			.filter(sym -> sym.offset() <= caret)
			.max(Comparator.comparingInt(Symbol::offset))
			.map(Symbol::offset)
			.orElse(0);
	}

	private int nextContainerStart(List<Symbol> symbols, int caret, int currentStart) {
		return symbols.stream()
			.filter(sym -> sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.DEFINITION
				|| sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.GROUP
				|| sym.kind() == org.logoce.lmf.editorfx.symbol.SymbolKind.METAMODEL)
			.map(Symbol::offset)
			.filter(off -> off > currentStart && off > caret)
			.min(Integer::compare)
			.orElse(-1);
	}

	private List<String> referenceNames(String text, int caret) {
		if (text == null || text.isEmpty()) return List.of();
		final String token = referenceToken(text, caret);
		if (token.isEmpty()) return List.of();
		final List<String> names = new ArrayList<>();
		final var idxMatcher = java.util.regex.Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)\\.(\\d+)").matcher(token);
		if (idxMatcher.find()) {
			names.add(idxMatcher.group(1));
		}
		final int hash = token.indexOf('#');
		if (hash >= 0 && hash + 1 < token.length()) {
			int i = hash + 1;
			final StringBuilder sb = new StringBuilder();
			while (i < token.length() && Character.isJavaIdentifierPart(token.charAt(i))) {
				sb.append(token.charAt(i++));
			}
			if (!sb.isEmpty()) names.add(sb.toString());
		}
		final int at = token.indexOf('@');
		if (at >= 0 && at + 1 < token.length()) {
			int i = at + 1;
			final StringBuilder sb = new StringBuilder();
			while (i < token.length() && Character.isJavaIdentifierPart(token.charAt(i))) {
				sb.append(token.charAt(i++));
			}
			if (!sb.isEmpty()) names.add(0, sb.toString()); // prefer @ target first
		}
		// fallback: last identifier-like segment in the token (handles ../generics.0)
		final var matcher = java.util.regex.Pattern.compile("([A-Za-z_][A-Za-z0-9_]*)").matcher(token);
		String lastId = null;
		while (matcher.find()) {
			lastId = matcher.group(1);
		}
		if (lastId != null) {
			names.add(lastId);
		}
		return names.stream().filter(s -> !s.isEmpty()).distinct().toList();
	}

	private record RefSlice(String token, int start, int end) {}

	private String referenceToken(String text, int caret) {
		return referenceSlice(text, caret).token();
	}

	private RefSlice referenceSlice(String text, int caret) {
		if (text == null) return new RefSlice("", caret, caret);
		int start = caret;
		while (start > 0 && !Character.isWhitespace(text.charAt(start - 1)) && text.charAt(start - 1) != '(' && text.charAt(start - 1) != ')') start--;
		int end = caret;
		while (end < text.length() && !Character.isWhitespace(text.charAt(end)) && text.charAt(end) != '(' && text.charAt(end) != ')') end++;
		if (start >= end) return new RefSlice("", caret, caret);
		return new RefSlice(text.substring(start, end), start, end);
	}

	private void updateReferencesAtCaret(EditorTabState state) {
		if (state == null) {
			referencesPane.setReferences(List.of());
			return;
		}
		final int caret = state.area().getCaretPosition();
		final var slice = referenceSlice(state.area().getText(), caret);
		final String token = slice.token();
		final var names = new ArrayList<>(referenceNames(state.area().getText(), caret));
		final String word = TextOps.wordAt(state.area().getText(), caret);
		if (word != null && !word.isEmpty()) names.add(word);
		if (names.isEmpty()) {
			referencesPane.setReferences(List.of());
			referenceHighlighter.clear(state.document().path());
			return;
		}
		final var refs = referenceHighlighter.update(state.document(), state.area(), names, token, nameResolver, modelService, slice.start(), slice.end());
		referencesPane.setReferences(refs);
		applyHighlight(state.area(), state.area().getText(), state);
	}

	private void recordLocation(EditorTabState state) {
		recordLocation(state, false);
	}

	private void recordLocation(EditorTabState state, boolean clearForward) {
		if (state == null) return;
		navigationService.record(state, clearForward);
	}

	private void showCompletion() {
		final var state = currentState();
		if (state == null) return;
		final var caret = state.area().getCaretPosition();
		final String prefix = TextOps.wordAt(state.area().getText(), caret);
		final List<String> suggestions = new ArrayList<>();
		suggestions.addAll(KEYWORD_COMPLETIONS);
		state.document().symbols().forEach(s -> suggestions.add(s.name()));
		final var filtered = suggestions.stream()
			.filter(s -> prefix == null || prefix.isEmpty() || s.startsWith(prefix))
			.distinct()
			.sorted()
			.toList();
		if (filtered.isEmpty()) return;
		final var menu = new ContextMenu();
		for (String s : filtered) {
			final var item = new MenuItem(s);
			item.setOnAction(ev -> {
				TextOps.replaceWordAtCaret(state.area(), s);
			});
			menu.getItems().add(item);
		}
		menu.show(tabPane.getScene().getWindow());
	}

	private void findReferences(boolean focusPane) {
		final var state = currentState();
		if (state == null) return;
		final int caret = state.area().getCaretPosition();
		final var names = referenceNames(state.area().getText(), caret);
		final String word = TextOps.wordAt(state.area().getText(), caret);
		if (word != null && !word.isEmpty()) names.add(word);
		if (names.isEmpty()) return;
		final int containerStart = nearestContainerStart(state.document().symbols(), caret);
		final int containerEnd = nextContainerStart(state.document().symbols(), caret, containerStart);
		final var refs = ReferenceFinder.findReferences(state.document(), names.getFirst(), containerStart, containerEnd);
		referencesPane.setReferences(refs);
		if (focusPane) {
			referencesPane.buildNode().requestFocus();
		}
		log("Found " + refs.size() + " references for " + names.getFirst());
	}

	private void renameSymbol() {
		final var state = currentState();
		if (state == null) return;
		final int caret = state.area().getCaretPosition();
		final String oldName = TextOps.wordAt(state.area().getText(), caret);
		if (oldName == null || oldName.isBlank()) {
			log("No symbol under caret");
			return;
		}
		final var dialog = new TextInputDialog(oldName);
		dialog.setTitle("Rename");
		dialog.setHeaderText("Rename symbol '" + oldName + "'");
		final var result = dialog.showAndWait();
		if (result.isEmpty()) return;
		final String newName = result.get().strip();
		if (newName.isEmpty() || newName.equals(oldName)) return;

		int total = 0;
		for (var doc : documentManager.documents()) {
			final var refs = ReferenceFinder.findReferences(doc, oldName);
			if (refs.isEmpty()) continue;
			final var sb = new StringBuilder(doc.getText());
			refs.stream()
				.sorted((a, b) -> Integer.compare(b.offset(), a.offset()))
				.forEach(ref -> sb.replace(ref.offset(), ref.offset() + ref.length(), newName));
			doc.setText(sb.toString());
			total += refs.size();
			syncOpenEditor(doc);
			updateFileListDirty(doc);
		}
		log("Renamed '" + oldName + "' to '" + newName + "' (" + total + " occurrences)");
	}

	private void syncOpenEditor(Document doc) {
		for (var tab : tabPane.getTabs()) {
			if (tab.getUserData() instanceof EditorTabState st && st.document().path().equals(doc.path())) {
				st.area().replaceText(doc.getText());
				applyHighlight(st.area(), doc.getText(), st);
				updateTabLabel(tab, st);
				refreshSymbolsView(st);
			}
		}
	}

	private void updateFileListDirty(Document doc) {
		workspaceBrowser.updateDirty(doc);
	}
}
