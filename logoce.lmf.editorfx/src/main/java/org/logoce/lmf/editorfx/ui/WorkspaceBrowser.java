package org.logoce.lmf.editorfx.ui;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import org.logoce.lmf.editorfx.core.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WorkspaceBrowser {
	private final ListView<BrowserRow> listView = new ListView<>();
	private final BorderPane node;
	private Consumer<Path> onOpen = ignored -> {};

	public WorkspaceBrowser() {
		listView.setMinWidth(260);
		listView.setCellFactory(list -> new BrowserRowCell());
		listView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				openSelected();
			}
		});
		listView.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				openSelected();
			}
		});

		final var title = new Label("Models");
		title.setStyle("-fx-font-weight: bold;");
		final var header = new HBox(title);
		header.setPadding(new Insets(6, 6, 6, 6));
		header.getStyleClass().add("pane-background");

		node = new BorderPane();
		node.setTop(header);
		node.setCenter(listView);
		node.setPrefWidth(320);
		node.getStyleClass().add("file-browser");
		node.setPadding(new Insets(0, 6, 0, 6));
		node.getStyleClass().add("pane-background");
	}

	public Node node() {
		return node;
	}

	public void setOnOpen(Consumer<Path> handler) {
		onOpen = handler != null ? handler : ignored -> {};
	}

	public void refresh(Path workspaceRoot, Collection<Document> openDocs, Consumer<String> logger) {
		final List<Path> files;
		try (Stream<Path> stream = Files.walk(workspaceRoot)) {
			files = stream
				.filter(Files::isRegularFile)
				.filter(path -> path.getFileName().toString().endsWith(".lm"))
				.sorted()
				.toList();
		} catch (IOException e) {
			if (logger != null) logger.accept("Failed to scan workspace: " + e.getMessage());
			return;
		}

		final Map<Path, List<Path>> byFolder = new HashMap<>();
		for (var file : files) {
			final Path parent = Optional.ofNullable(file.getParent()).orElse(workspaceRoot);
			byFolder.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(file);
		}

		final List<BrowserRow> rows = byFolder.entrySet().stream()
			.sorted(Comparator.comparing(entry -> entry.getKey().toString()))
			.flatMap(entry -> {
				final Path folder = entry.getKey();
				final String display = toDisplayPath(workspaceRoot, folder);
				final List<BrowserRow> bundle = new ArrayList<>();
				bundle.add(new FolderRow(folder, display));
				bundle.addAll(entry.getValue().stream()
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.map(path -> {
						final FileRow row = new FileRow(folder, path);
						openDocs.stream()
							.filter(doc -> doc.path().equals(path))
							.findFirst()
							.ifPresent(doc -> row.setModified(doc.isDirty()));
						return row;
					})
					.toList());
				return bundle.stream();
			})
			.collect(Collectors.toList());

		listView.setItems(FXCollections.observableArrayList(rows));
		if (logger != null) logger.accept("Workspace refreshed (" + files.size() + " .lm files)");
	}

	public void select(Path path) {
		if (path == null) return;
		for (int i = 0; i < listView.getItems().size(); i++) {
			final BrowserRow row = listView.getItems().get(i);
			if (row instanceof FileRow fr && fr.path().equals(path)) {
				listView.getSelectionModel().select(i);
				listView.scrollTo(i);
				return;
			}
		}
	}

	public void updateDirty(Document doc) {
		for (var row : listView.getItems()) {
			if (row instanceof FileRow fr && fr.path().equals(doc.path())) {
				fr.setModified(doc.isDirty());
				listView.refresh();
				return;
			}
		}
	}

	private void openSelected() {
		final BrowserRow selected = listView.getSelectionModel().getSelectedItem();
		if (selected instanceof FileRow fileRow) {
			onOpen.accept(fileRow.path());
		}
	}

	private static String toDisplayPath(Path workspaceRoot, Path folder) {
		if (folder.equals(workspaceRoot)) {
			return workspaceRoot.getFileName() != null ? workspaceRoot.getFileName().toString() : workspaceRoot.toString();
		}
		return workspaceRoot.relativize(folder).toString();
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

	private static final class BrowserRowCell extends ListCell<BrowserRow> {
		@Override
		protected void updateItem(BrowserRow item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setText(null);
				setStyle(null);
				return;
			}
			if (item instanceof FolderRow folderRow) {
				setText(folderRow.label());
				setStyle("-fx-font-weight: bold;");
			} else if (item instanceof FileRow fileRow) {
				final String name = fileRow.path().getFileName().toString();
				final boolean modified = fileRow.modified();
				final String star = modified ? " *" : "";
				setText("  - " + name + star);
				setStyle(modified ? "-fx-font-weight: bold;" : null);
			}
		}
	}
}
