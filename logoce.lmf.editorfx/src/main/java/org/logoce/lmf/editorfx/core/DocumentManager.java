package org.logoce.lmf.editorfx.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class DocumentManager {
	private final Map<Path, Document> documents = new ConcurrentHashMap<>();

	public Document getOrCreate(Path path, String initialText, Consumer<Document> onChange) {
		return documents.computeIfAbsent(path.toAbsolutePath().normalize(), ignored -> {
			final Document doc = new Document(path, initialText);
			doc.setChangeListener(onChange);
			return doc;
		});
	}

	public Document load(Path path, Consumer<Document> onChange) throws IOException {
		final Path normalized = path.toAbsolutePath().normalize();
		final String content = Files.readString(normalized, StandardCharsets.UTF_8);
		return documents.compute(normalized, (p, existing) -> {
			if (existing == null) {
				final Document doc = new Document(normalized, content);
				doc.setChangeListener(onChange);
				return doc;
			}
			existing.resetContent(content);
			existing.setChangeListener(onChange);
			return existing;
		});
	}

	public void save(Document document) throws IOException {
		Files.writeString(document.path(), document.getText(), StandardCharsets.UTF_8);
		document.markSaved();
	}

	public Collection<Document> documents() {
		return Collections.unmodifiableCollection(documents.values());
	}
}
