package org.logoce.lmf.editorfx.nav;

import org.logoce.lmf.editorfx.EditorApp.EditorTabState;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class NavigationService {
	private final NavigationHistory history;
	private final Supplier<EditorTabState> currentState;
	private final BiConsumer<Path, Integer> navigator;

	public NavigationService(NavigationHistory history, Supplier<EditorTabState> currentState, BiConsumer<Path, Integer> navigator) {
		this.history = history;
		this.currentState = currentState;
		this.navigator = navigator;
	}

	public void record(EditorTabState state, boolean clearForward) {
		if (state == null) return;
		final int caret = state.area().getCaretPosition();
		history.record(state.document().path(), caret, clearForward);
	}

	public void navigateBack() {
		final var current = currentState.get();
		final var target = history.back(current != null ? current.document().path() : null, current != null ? current.area().getCaretPosition() : 0);
		target.ifPresent(entry -> navigator.accept(entry.path(), entry.caret()));
	}

	public void navigateForward() {
		final var current = currentState.get();
		final var target = history.forward(current != null ? current.document().path() : null, current != null ? current.area().getCaretPosition() : 0);
		target.ifPresent(entry -> navigator.accept(entry.path(), entry.caret()));
	}
}
