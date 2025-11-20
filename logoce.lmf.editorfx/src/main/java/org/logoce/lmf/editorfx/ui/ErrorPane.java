package org.logoce.lmf.editorfx.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.logoce.lmf.editorfx.diagnostic.Diagnostic;

import java.util.Collection;

public final class ErrorPane {
	private final ListView<Diagnostic> listView = new ListView<>();
	private final Node node;

	public ErrorPane() {
		listView.setCellFactory(list -> new ListCell<>() {
			@Override
			protected void updateItem(Diagnostic item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.path().getFileName() + ":" + item.line() + ":" + item.column() + " [" + item.severity() + "] " + item.message());
				}
			}
		});

		final var title = new Label("Errors");
		title.setStyle("-fx-font-weight: bold;");
		final var header = new HBox(title);
		header.setPadding(new Insets(6, 6, 6, 6));
		header.getStyleClass().add("pane-background");

		final var container = new BorderPane();
		container.setTop(header);
		container.setCenter(listView);
		container.setPrefHeight(180);
		container.getStyleClass().add("pane-background");
		container.setPadding(new Insets(6, 6, 6, 6));
		this.node = container;
	}

	public Node node() {
		return node;
	}

	public void setDiagnostics(Collection<Diagnostic> diags) {
		listView.setItems(FXCollections.observableArrayList(diags));
	}
}
