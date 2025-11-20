package org.logoce.lmf.editorfx.symbol;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;

import java.util.Collection;
import java.util.function.Consumer;

public final class SymbolPane {
	private final ListView<Symbol> listView = new ListView<>();
	private Consumer<Symbol> onJump = ignored -> {};

	public SymbolPane() {
		listView.setPrefWidth(220);
		listView.setCellFactory(list -> new ListCell<>() {
			@Override
			protected void updateItem(Symbol item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					final var kind = new Label(item.kind().name().toLowerCase());
					kind.getStyleClass().add("symbol-kind");
					final var spacer = new Region();
					HBox.setHgrow(spacer, Priority.ALWAYS);
					final var name = new Label(item.name());
					name.setStyle("-fx-font-weight: normal;");
					final var box = new HBox(6, kind, spacer, name);
					setText(null);
					setGraphic(box);
				}
			}
		});
		listView.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) {
				jumpToSelected();
			}
		});
		listView.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				jumpToSelected();
			}
		});
	}

	public void setSymbols(Collection<Symbol> symbols) {
		listView.setItems(FXCollections.observableArrayList(symbols));
	}

	public void setOnJump(Consumer<Symbol> handler) {
		this.onJump = handler != null ? handler : ignored -> {};
	}

	public Node buildNode() {
		final var title = new Label("Symbols");
		title.setStyle("-fx-font-weight: bold;");
		final var header = new HBox(title);
		header.setPadding(new Insets(6, 6, 6, 6));
		header.getStyleClass().add("pane-background");

		final var container = new BorderPane();
		container.setTop(header);
		container.setCenter(listView);
		container.setPrefWidth(240);
		container.getStyleClass().add("pane-background");
		container.setPadding(new Insets(0, 6, 0, 6));
		return container;
	}

	private void jumpToSelected() {
		final var symbol = listView.getSelectionModel().getSelectedItem();
		if (symbol != null) {
			onJump.accept(symbol);
		}
	}
}
