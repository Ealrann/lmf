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
import org.logoce.lmf.editorfx.symbol.Reference;

import java.util.Collection;
import java.util.function.Consumer;

public final class ReferencesPane {
	private final ListView<Reference> listView = new ListView<>();
	private Consumer<Reference> onJump = ignored -> {};
	private final Node node;

	public ReferencesPane() {
		listView.setPrefHeight(180);
		listView.setCellFactory(list -> new ListCell<>() {
			@Override
			protected void updateItem(Reference item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(item.path().getFileName() + ":" + item.line() + ":" + item.column() + " - " + item.name());
				}
			}
		});
		listView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				jump();
			}
		});
		listView.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				jump();
			}
		});

		final var title = new Label("References");
		title.setStyle("-fx-font-weight: bold;");
		final var header = new HBox(title);
		header.setPadding(new Insets(6, 6, 6, 6));
		header.getStyleClass().add("pane-background");

		final var container = new BorderPane();
		container.setTop(header);
		container.setCenter(listView);
		container.getStyleClass().add("pane-background");
		container.setPadding(new Insets(6, 6, 6, 6));
		this.node = container;
	}

	public void setReferences(Collection<Reference> refs) {
		listView.setItems(FXCollections.observableArrayList(refs));
	}

	public void setOnJump(Consumer<Reference> handler) {
		onJump = handler != null ? handler : ignored -> {};
	}

	public Node buildNode() {
		return node;
	}

	private void jump() {
		final var ref = listView.getSelectionModel().getSelectedItem();
		if (ref != null) {
			onJump.accept(ref);
		}
	}
}
