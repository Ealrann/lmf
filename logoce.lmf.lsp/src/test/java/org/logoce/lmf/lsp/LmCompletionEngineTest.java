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

final class LmCompletionEngineTest
{
	@Test
	void groupHeaderOffersGroupFeatures()
	{
		final var text = """
			(MetaModel domain=test.model name=Test
			    (Group LMObject)
			    (Group Entity
			        (includes group=@LMObject))
			)
			""";

		final var uri = URI.create("file:///test/GroupFeatures.lm");
		final var position = positionAt(text, "Entity");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected some completion items for Group header");
		assertTrue(items.stream().anyMatch(i -> "concrete".equals(i.getLabel())),
				   "Expected 'concrete' feature in Group header completions");
	}

	@Test
	void groupHeaderDoesNotOfferAlreadyDefinedFeatures()
	{
		final var text = """
			(MetaModel domain=test.model3 name=TestDefined
			    (Group LMObject)
			    (Group Entity concrete=true
			        (includes group=@LMObject))
			)
			""";

		final var uri = URI.create("file:///test/GroupDefinedFeatures.lm");
		final var position = positionAfter(text, "Group Entity ");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected some completion items for Group header with existing features");
		assertTrue(items.stream().noneMatch(i -> "concrete".equals(i.getLabel())),
				   "Expected 'concrete' to be filtered out from Group header completions once defined");
	}

	@Test
	void groupHeaderAfterNameOffersGroupFeatures()
	{
		final var text = """
			(MetaModel domain=test.model name=Test2
			    (Group LMObject)
			    (Group Entity
			    )
			)
			""";

		final var uri = URI.create("file:///test/GroupFeaturesAfterName.lm");
		final var position = positionAfter(text, "Group Entity");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected completion items after Group name");
		assertTrue(items.stream().anyMatch(i -> "concrete".equals(i.getLabel())),
				   "Expected 'concrete' feature in Group header completions after name");
	}

	@Test
	void booleanAttributeCompletionOffersTrueFalse()
	{
		final var text = """
			(MetaModel domain=test.bool name=BoolTest
			    (Group LMObject)
			    (Group Entity concrete=
			        (includes group=@LMObject))
			)
			""";

		final var uri = URI.create("file:///test/BoolTest.lm");
		final var position = positionAfter(text, "concrete=");

		final var items = complete(uri, text, position);

		assertTrue(items.stream().anyMatch(i -> "true".equals(i.getLabel())),
				   "Expected 'true' in boolean attribute completions for 'concrete='");
		assertTrue(items.stream().anyMatch(i -> "false".equals(i.getLabel())),
				   "Expected 'false' in boolean attribute completions for 'concrete='");
	}

	@Test
	void relationValueCompletionOffersNamedGroups()
		throws Exception
	{
		final var text = readLmCoreSource();
		final var uri = URI.create("file:///home/eal/git/LMF/logoce.lmf.model/src/main/model/asset/LMCore.lm");

		final var position = positionAfter(text, "group=@");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected relation value completions for 'group=@...'");
		assertTrue(items.stream().anyMatch(i -> "@Named".equals(i.getLabel())),
				   "Expected '@Named' among relation value completions for 'group=@...'");
	}

	@Test
	void groupHeaderCompletionMatchesOperationsGenericCase()
		throws Exception
	{
		Path path = Path.of("logoce.lmf.generator/src/test/model/OperationsGeneric.lm");
		if (!Files.exists(path))
		{
			path = Path.of("../logoce.lmf.generator/src/test/model/OperationsGeneric.lm");
		}

		final var text = Files.readString(path, StandardCharsets.UTF_8);
		final var uri = path.toAbsolutePath().toUri();

		final var position = positionAfter(text, "(Group Test");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected completion items for '(Group Test ...)' header");
		assertTrue(items.stream().anyMatch(i -> "concrete".equals(i.getLabel())),
				   "Expected 'concrete' feature in Group header completions for '(Group Test ...)'.");
	}

	@Test
	void containmentFeatureCompletionOffersAttributeAndRelation()
	{
		final var text = """
			(MetaModel domain=test.containment name=Containment
			    (Group Entity)
			    (Definition Car
			        (includes group=@Entity)

			    )
			)
			""";

		final var uri = URI.create("file:///test/ContainmentFeatures.lm");
		final var position = positionAfter(text, "(includes group=@Entity)\n");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected completion items in containment position inside Group body");
		assertTrue(items.stream().anyMatch(i -> "(features:Attribute )".equals(i.getLabel())),
				   "Expected '(features:Attribute )' among containment child completions");
		assertTrue(items.stream().anyMatch(i -> "(features:Relation )".equals(i.getLabel())),
				   "Expected '(features:Relation )' among containment child completions");
	}

	@Test
	void groupHeaderBlankLineOffersGroupFeatures()
	{
		final var text = """
			(MetaModel domain=test.blankline name=Blank
			    (Group LMObject)
			    (Group Entity

			        (includes group=@LMObject))
			)
			""";

		final var uri = URI.create("file:///test/GroupHeaderBlankLine.lm");
		final var position = positionAfter(text, "Group Entity\n");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected completion items on blank line after Group header");
		assertTrue(items.stream().anyMatch(i -> "concrete".equals(i.getLabel())),
				   "Expected 'concrete' feature in Group header completions on blank line after header");
	}

	@Test
	void localAtTypeCompletionOffersMetaModelTypes()
	{
		final var text = """
			(MetaModel domain=test.types name=TestTypes
			    (Group Entity)
			    @
			)
			""";

		final var uri = URI.create("file:///test/TypeCompletionLocalAt.lm");
		final var position = positionAt(text, "@");

		final var items = complete(uri, text, position);

		assertFalse(items.isEmpty(), "Expected type completions at '@' position");
		// Debug output to understand available labels in this context.
		for (final var item : items)
		{
			System.err.println("localAtTypeCompletionOffersMetaModelTypes completion: " + item.getLabel() +
							   " detail=" + item.getDetail() +
							   " insertText=" + item.getInsertText());
		}
		assertTrue(items.stream().anyMatch(i -> "Entity".equals(i.getLabel())),
				   "Expected local type completion to include 'Entity' group from active meta-model");
	}

	private static List<CompletionItem> complete(final URI uri,
												 final String text,
												 final Position position)
	{
		final var server = new LmLanguageServer();
		try
		{
			final var index = server.workspaceIndex();
			final var state = new LmDocumentState(uri, 1, text);
			index.putDocument(state);

			server.rebuildWorkspace();

			final Either<List<CompletionItem>, CompletionList> result =
				LmCompletionEngine.complete(server, uri, position);

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

	private static Position positionAt(final String text, final String marker)
	{
		final int offset = text.indexOf(marker);
		if (offset < 0)
		{
			throw new IllegalArgumentException("Marker not found: " + marker);
		}
		return positionForOffset(text, offset);
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

	private static String readLmCoreSource() throws Exception
	{
		Path path = Path.of("logoce.lmf.model/src/main/model/asset/LMCore.lm");
		if (!Files.exists(path))
		{
			path = Path.of("../logoce.lmf.model/src/main/model/asset/LMCore.lm");
		}
		return Files.readString(path, StandardCharsets.UTF_8);
	}
}
