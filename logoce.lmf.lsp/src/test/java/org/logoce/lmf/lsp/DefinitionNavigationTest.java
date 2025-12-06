package org.logoce.lmf.lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.Test;
import org.logoce.lmf.lsp.state.LmDocumentState;
import org.logoce.lmf.lsp.state.LmSymbolKind;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class DefinitionNavigationTest
{
	@Test
	void goToDefinitionFromLocalAtReferenceInMetaModel() throws Exception
	{
		final var text = readLmCoreSource();
		final var uri = Path.of("../logoce.lmf.model/src/main/model/asset/LMCore.lm").toAbsolutePath().toUri();

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final var state = new LmDocumentState(uri, 1, text);
		server.workspaceIndex().putDocument(state);
		server.worker().submit(server::rebuildWorkspace).get();

		final var position = positionAt(text, "@string");

		// Ensure we have at least one reference recorded for this document.
		final var refs = server.workspaceIndex().referencesForUri(uri);
		System.err.println("goToDefinitionFromLocalAtReferenceInMetaModel refs.size=" + refs.size());
		for (final var ref : refs)
		{
			System.err.println("LMCore ref: target=" + ref.target().name() +
							   " uri=" + ref.uri() +
							   " range=" + ref.range());
		}
		org.junit.jupiter.api.Assertions.assertFalse(refs.isEmpty(), "Expected non-empty references for LMCore.lm");

		final var id = server.findTargetSymbol(uri, position);
		assertNotNull(id, "Expected a target symbol at '@string' reference");
		assertEquals(LmSymbolKind.TYPE, id.kind(), "Expected TYPE symbol for '@string' reference");
		assertEquals("string", id.name(), "Expected target type name 'string' for '@string' reference");

		server.shutdown().join();
	}

	@Test
	void goToDefinitionFromLocalAtReferenceInM1Model() throws Exception
	{
		Path path = Path.of("../logoce.lmf.generator/src/test/model/CarCompany.lm");
		final String text = Files.readString(path, StandardCharsets.UTF_8);
		final URI uri = path.toAbsolutePath().toUri();

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final var state = new LmDocumentState(uri, 1, text);
		server.workspaceIndex().putDocument(state);
		server.worker().submit(server::rebuildWorkspace).get();

		// '@Car' inside '(+contains cars [0..*] @Car)'
		final var position = positionAt(text, "@Car");

		final var refs = server.workspaceIndex().referencesForUri(uri);
		System.err.println("goToDefinitionFromLocalAtReferenceInM1Model refs.size=" + refs.size());
		for (final var ref : refs)
		{
			System.err.println("CarCompany ref: target=" + ref.target().name() +
							   " uri=" + ref.uri() +
							   " range=" + ref.range());
		}
		org.junit.jupiter.api.Assertions.assertFalse(refs.isEmpty(), "Expected non-empty references for CarCompany.lm");

		final var id = server.findTargetSymbol(uri, position);
		assertNotNull(id, "Expected a target symbol at '@Car' reference");
		assertEquals(LmSymbolKind.TYPE, id.kind(), "Expected TYPE symbol for '@Car' reference");
		assertEquals("Car", id.name(), "Expected target type name 'Car' for '@Car' reference");

		server.shutdown().join();
	}

	@Test
	void goToDefinitionFromMetaModelImportsEntry() throws Exception
	{
		final Path corePath = Path.of("../logoce.lmf.generator/src/test/model/GraphCore.lm");
		final Path extensionsPath = Path.of("../logoce.lmf.generator/src/test/model/GraphExtensions.lm");

		final String coreText = Files.readString(corePath, StandardCharsets.UTF_8);
		final String extensionsText = Files.readString(extensionsPath, StandardCharsets.UTF_8);

		final URI coreUri = corePath.toAbsolutePath().toUri();
		final URI extensionsUri = extensionsPath.toAbsolutePath().toUri();

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final var coreState = new LmDocumentState(coreUri, 1, coreText);
		final var extensionsState = new LmDocumentState(extensionsUri, 1, extensionsText);
		server.workspaceIndex().putDocument(coreState);
		server.workspaceIndex().putDocument(extensionsState);

		server.worker().submit(server::rebuildWorkspace).get();

		final Position importsRef = positionAt(extensionsText, "test.multi.GraphCore");
		final var id = server.findTargetSymbol(extensionsUri, importsRef);

		assertNotNull(id, "Expected a target symbol at 'test.multi.GraphCore' import reference");
		assertEquals(LmSymbolKind.META_MODEL, id.kind(), "Expected META_MODEL symbol for import reference");
		assertEquals("test.multi.GraphCore", id.modelKey().qualifiedName(), "Expected GraphCore meta-model as target");

		server.shutdown().join();
	}

	@Test
	void goToDefinitionFromMetamodelsEntryInM1Model() throws Exception
	{
		Path carCompanyPath = Path.of("../logoce.lmf.model/src/test/model/CarCompany.lm");
		if (!Files.exists(carCompanyPath))
		{
			carCompanyPath = Path.of("logoce.lmf.model/src/test/model/CarCompany.lm");
		}
		final Path peugeotPath = Path.of("../logoce.lmf.model/src/test/model/Peugeot.lm");

		final String carCompanyText = Files.readString(carCompanyPath, StandardCharsets.UTF_8);
		final String peugeotText = Files.readString(peugeotPath, StandardCharsets.UTF_8);

		final URI carCompanyUri = carCompanyPath.toAbsolutePath().toUri();
		final URI peugeotUri = peugeotPath.toAbsolutePath().toUri();

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final var carCompanyState = new LmDocumentState(carCompanyUri, 1, carCompanyText);
		final var peugeotState = new LmDocumentState(peugeotUri, 1, peugeotText);
		server.workspaceIndex().putDocument(carCompanyState);
		server.workspaceIndex().putDocument(peugeotState);

		server.worker().submit(server::rebuildWorkspace).get();

		final Position metamodelRef = positionAt(peugeotText, "test.model.CarCompany");
		final var id = server.findTargetSymbol(peugeotUri, metamodelRef);

		assertNotNull(id, "Expected a target symbol at 'test.model.CarCompany' metamodels reference");
		assertEquals(LmSymbolKind.META_MODEL, id.kind(), "Expected META_MODEL symbol for metamodels reference");
		assertEquals("test.model.CarCompany", id.modelKey().qualifiedName(), "Expected CarCompany meta-model as target");

		server.shutdown().join();
	}

	@Test
	void goToDefinitionNavigatesToNameToken() throws Exception
	{
		Path path = Path.of("logoce.lmf.model/src/test/model/CarCompany.lm");
		if (!Files.exists(path))
		{
			path = Path.of("../logoce.lmf.model/src/test/model/CarCompany.lm");
		}
		final String text = Files.readString(path, StandardCharsets.UTF_8);
		final URI uri = path.toAbsolutePath().toUri();

		final var server = new LmLanguageServer();
		server.connect(new NoopClient());

		final var state = new LmDocumentState(uri, 1, text);
		server.workspaceIndex().putDocument(state);
		server.worker().submit(server::rebuildWorkspace).get();

		// '@Person' inside '(+contains ceo @Person [1..1])'
		final var atRef = positionAt(text, "@Person");

		final var textDocumentService = (LmTextDocumentService) server.getTextDocumentService();
		final var params = new DefinitionParams();
		params.setTextDocument(new TextDocumentIdentifier(uri.toString()));
		params.setPosition(atRef);

		final Either<java.util.List<? extends Location>, java.util.List<? extends org.eclipse.lsp4j.LocationLink>> result =
			textDocumentService.definition(params).get();

		assertTrue(result.isLeft(), "Expected simple Location result for definition");
		final var locations = result.getLeft();
		assertNotNull(locations, "Locations should not be null");
		assertFalse(locations.isEmpty(), "Expected at least one definition location");

		final Location loc = locations.getFirst();
		assertEquals(uri.toString(), loc.getUri(), "Definition should be in the same document");

		final Position start = loc.getRange().getStart();

		// Expected position: the 'Person' token in '(Definition Person ...)'
		final String header = "(Definition Person";
		final int headerOffset = text.indexOf(header);
		assertTrue(headerOffset >= 0, "Header '(Definition Person' should exist in CarCompany.lm");
		final int nameOffset = headerOffset + "(Definition ".length();
		final Position expected = positionForOffset(text, nameOffset);

		assertEquals(expected.getLine(), start.getLine(), "Definition line should match 'Person' token");
		assertEquals(expected.getCharacter(), start.getCharacter(), "Definition column should match 'Person' token");

		server.shutdown().join();
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

	private static Position positionAt(final String text, final String marker)
	{
		final int offset = text.indexOf(marker);
		if (offset < 0)
		{
			throw new IllegalArgumentException("Marker not found: " + marker);
		}
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
