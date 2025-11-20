package org.logoce.lmf.editorfx.nav;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public final class NavigationHistory {
	private final Deque<NavEntry> back = new ArrayDeque<>();
	private final Deque<NavEntry> forward = new ArrayDeque<>();

	public void record(Path path, int caret, boolean clearForward) {
		if (path == null) return;
		final NavEntry entry = new NavEntry(path, caret);
		if (back.isEmpty() || !back.getLast().equals(entry)) {
			back.add(entry);
		}
		if (clearForward) {
			forward.clear();
		}
	}

	public Optional<NavEntry> back(Path currentPath, int currentCaret) {
		if (back.isEmpty()) return Optional.empty();
		if (currentPath != null) {
			forward.add(new NavEntry(currentPath, currentCaret));
		}
		final NavEntry target = back.pollLast();
		return Optional.ofNullable(target);
	}

	public Optional<NavEntry> forward(Path currentPath, int currentCaret) {
		if (forward.isEmpty()) return Optional.empty();
		if (currentPath != null) {
			back.add(new NavEntry(currentPath, currentCaret));
		}
		final NavEntry target = forward.pollLast();
		return Optional.ofNullable(target);
	}

	public record NavEntry(Path path, int caret) {}
}
