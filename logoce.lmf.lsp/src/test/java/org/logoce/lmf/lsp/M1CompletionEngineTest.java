package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.features.completion.LmCompletionEngine;
import org.logoce.lmf.lsp.state.LmDocumentState;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class M1CompletionEngineTest
{
	@Test
	void m1ContainmentCompletionOffersCarFromMetaModel() throws Exception
	{
		final Path carCompanyPath = resolvePath("logoce.lmf.model/src/test/model/CarCompany.lm");
		final String carCompanyText = Files.readString(carCompanyPath, StandardCharsets.UTF_8);

		final String m1Text = """
			(CarCompany domain=test.model name=PeugeotCompany metamodels=test.model.CarCompany
			    (CarParc
			        (
			    )
			)
			)
			""";

		final URI m2Uri = carCompanyPath.toAbsolutePath().toUri();
		final URI m1Uri = URI.create("file:///test/PeugeotM1Containment.lm");
		final Position position = positionAfter(m1Text, "(CarParc\n        ");

		final var items = completeWithDocs(
			List.of(
				new Doc(m2Uri, carCompanyText),
				new Doc(m1Uri, m1Text)),
			m1Uri,
			position);

		assertFalse(items.isEmpty(), "Expected completion items in M1 containment position");
		assertTrue(items.stream().anyMatch(i -> "(cars:Car )".equals(i.getLabel())),
				   "Expected '(cars:Car )' among M1 containment child completions based on CarCompany meta-model");
	}

	@Test
	void m1ContainmentCompletionAddsMandatoryFeaturesToInsertText() throws Exception
	{
		final Path carCompanyPath = resolvePath("logoce.lmf.model/src/test/model/CarCompany.lm");
		final String carCompanyText = Files.readString(carCompanyPath, StandardCharsets.UTF_8);

		final String m1Text = """
			(CarCompany domain=test.model name=PeugeotCompany metamodels=test.model.CarCompany
			    (CarParc
			        (
			    )
			)
			)
			""";

		final URI m2Uri = carCompanyPath.toAbsolutePath().toUri();
		final URI m1Uri = URI.create("file:///test/PeugeotM1ContainmentMandatory.lm");
		final Position position = positionAfter(m1Text, "(CarParc\n        ");

		final var items = completeWithDocs(
			List.of(
				new Doc(m2Uri, carCompanyText),
				new Doc(m1Uri, m1Text)),
			m1Uri,
			position);

		final var candidate = items.stream()
								   .filter(i -> "(cars:Car )".equals(i.getLabel()))
								   .findFirst()
								   .orElseThrow(() -> new AssertionError("Expected '(cars:Car )' completion item"));

		final String insertText = candidate.getInsertText();
		// For the Car group, 'name' (from Entity/Named) and 'brand' are mandatory.
		org.junit.jupiter.api.Assertions.assertNotNull(insertText, "Insert text should be set for '(cars:Car )' item");
		org.junit.jupiter.api.Assertions.assertTrue(insertText.startsWith("(cars "),
												   "Insert text should start with '(cars '");
		org.junit.jupiter.api.Assertions.assertTrue(insertText.contains("name="),
												   "Insert text should include mandatory 'name=' feature");
		org.junit.jupiter.api.Assertions.assertTrue(insertText.contains("brand="),
												   "Insert text should include mandatory 'brand=' feature");
	}

	@Test
	void m1RelationValueCompletionOffersCarFromMetaModel() throws Exception
	{
		final Path carCompanyPath = resolvePath("logoce.lmf.model/src/test/model/CarCompany.lm");
		final String carCompanyText = Files.readString(carCompanyPath, StandardCharsets.UTF_8);

		final String m1Text = """
			(CarCompany domain=test.model name=PeugeotCompany metamodels=test.model.CarCompany
			    (Person name=Macron car=@
			    )
			)
			""";

		final URI m2Uri = carCompanyPath.toAbsolutePath().toUri();
		final URI m1Uri = URI.create("file:///test/PeugeotM1Relation.lm");
		final Position position = positionAfter(m1Text, "car=@");

		final var items = completeWithDocs(
			List.of(
				new Doc(m2Uri, carCompanyText),
				new Doc(m1Uri, m1Text)),
			m1Uri,
			position);

		assertFalse(items.isEmpty(), "Expected relation value completions for M1 Person.car");
		assertTrue(items.stream().anyMatch(i -> "#CarCompany@Car".equals(i.getLabel())),
				   "Expected '#CarCompany@Car' among relation value completions for M1 Person.car");
	}

	private record Doc(URI uri, String text)
	{
	}

	private static List<CompletionItem> completeWithDocs(final List<Doc> docs,
														 final URI targetUri,
														 final Position position) throws Exception
	{
		final var server = new LmLanguageServer();
		try
		{
			server.connect(new NoopClient());

			for (final Doc doc : docs)
			{
				final var state = new LmDocumentState(doc.uri(), 1, doc.text());
				server.workspaceIndex().putDocument(state);
			}

			server.worker().submit(server::rebuildWorkspace).get();

			final Either<List<CompletionItem>, CompletionList> result =
				LmCompletionEngine.complete(server, targetUri, position);

			final List<CompletionItem> items = result.isLeft()
											   ? result.getLeft()
											   : result.getRight().getItems();
			return List.copyOf(items);
		}
		finally
		{
			server.shutdown().join();
		}
	}

	private static Path resolvePath(final String relative)
	{
		Path path = Path.of(relative);
		if (!Files.exists(path))
		{
			path = Path.of("..", relative);
		}
		return path;
	}

	private static Position positionAfter(final String text, final String marker)
	{
		final int start = text.indexOf(marker);
		if (start < 0)
		{
			throw new IllegalArgumentException("Marker not found: " + marker);
		}
		final int offset = start + marker.length();
		return positionForOffset(text, offset);
	}

	private static Position positionForOffset(final String text, final int offset)
	{
		int line = 0;
		int col = 0;
		for (int i = 0; i < offset && i < text.length(); i++)
		{
			final char c = text.charAt(i);
			if (c == '\n')
			{
				line++;
				col = 0;
			}
			else
			{
				col++;
			}
		}
		return new Position(line, col);
	}
}
