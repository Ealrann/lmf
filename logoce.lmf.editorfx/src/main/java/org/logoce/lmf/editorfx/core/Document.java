package org.logoce.lmf.editorfx.core;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.List;
import org.logoce.lmf.editorfx.semantic.SemanticModel;

public final class Document {
	private final Path path;
	private final StringProperty text;
	private final BooleanProperty dirty;
	private String originalText;
	private Consumer<Document> changeListener = ignored -> {};
	private List<org.logoce.lmf.editorfx.diagnostic.Diagnostic> diagnostics = List.of();
	private List<org.logoce.lmf.editorfx.symbol.Symbol> symbols = List.of();
	private SemanticModel semanticModel = SemanticModel.empty();

	public Document(Path path, String initialText) {
		this.path = path;
		this.text = new SimpleStringProperty(initialText);
		this.dirty = new SimpleBooleanProperty(false);
		this.originalText = initialText;
		this.text.addListener((obs, oldText, newText) -> {
			dirty.set(!newText.equals(originalText));
			changeListener.accept(this);
		});
	}

	public Path path() {
		return path;
	}

	public StringProperty textProperty() {
		return text;
	}

	public BooleanProperty dirtyProperty() {
		return dirty;
	}

	public String getText() {
		return text.get();
	}

	public void setText(String newText) {
		text.set(newText);
	}

	public boolean isDirty() {
		return dirty.get();
	}

	public void markSaved() {
		originalText = getText();
		dirty.set(false);
	}

	public void resetContent(String newContent) {
		originalText = newContent;
		text.set(newContent);
	}

	public void setChangeListener(Consumer<Document> listener) {
		this.changeListener = listener != null ? listener : ignored -> {};
	}

	public List<org.logoce.lmf.editorfx.diagnostic.Diagnostic> diagnostics() {
		return diagnostics;
	}

	public void setDiagnostics(List<org.logoce.lmf.editorfx.diagnostic.Diagnostic> diagnostics) {
		this.diagnostics = diagnostics != null ? diagnostics : List.of();
	}

	public List<org.logoce.lmf.editorfx.symbol.Symbol> symbols() {
		return symbols;
	}

	public void setSymbols(List<org.logoce.lmf.editorfx.symbol.Symbol> symbols) {
		this.symbols = symbols != null ? symbols : List.of();
	}

	public SemanticModel semanticModel() {
		return semanticModel;
	}

	public void setSemanticModel(SemanticModel semanticModel) {
		this.semanticModel = semanticModel != null ? semanticModel : SemanticModel.empty();
	}
}
