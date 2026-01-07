package org.logoce.lmf.cli;

import org.junit.jupiter.api.Test;
import org.logoce.lmf.cli.edit.FeatureValueSpanIndex;
import org.logoce.lmf.core.loader.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.loader.api.loader.diagnostic.LmDiagnostic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FeatureValueSpanIndexTest
{
	@Test
	void buildsValueSpansForNamedAndUnnamedFeatures()
	{
		final var source = "(Group Named domain=test.model tags=alpha, beta description=\"hello world\" empty=\"\")";
		final var reader = new LmTreeReader();
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var readResult = reader.read(source, diagnostics);

		assertTrue(diagnostics.isEmpty(), "diagnostics: " + diagnostics);
		final var root = readResult.roots().getFirst().data();
		final var index = FeatureValueSpanIndex.build(root.tokens(), source);

		final var unnamed = index.featuresByName(null).getFirst();
		assertEquals("Named", unnamed.values().getFirst().raw());

		final var domain = index.findValueSpan("domain", "test.model").orElseThrow();
		assertEquals("test.model", slice(source, domain.span().offset(), domain.span().length()));

		final var alpha = index.findValueSpan("tags", "alpha").orElseThrow();
		assertEquals("alpha", slice(source, alpha.span().offset(), alpha.span().length()));

		final var beta = index.findValueSpan("tags", "beta").orElseThrow();
		assertEquals("beta", slice(source, beta.span().offset(), beta.span().length()));

		final var description = index.findValueSpan("description", "hello world").orElseThrow();
		assertEquals("hello world", slice(source, description.span().offset(), description.span().length()));
		assertTrue(description.quoted(), "description should be parsed as quoted");

		final var descriptionFeature = index.featuresByName("description").getFirst();
		assertNotNull(descriptionFeature.assignmentSpan());
		assertEquals("description=\"hello world\"",
					 slice(source, descriptionFeature.assignmentSpan().offset(), descriptionFeature.assignmentSpan().length()));

		final var empty = index.findValueSpan("empty", "").orElseThrow();
		assertEquals(0, empty.span().length());
		assertEquals("\"\"", slice(source, empty.span().offset() - 1, 2));
	}

	private static String slice(final String source, final int offset, final int length)
	{
		return source.substring(offset, offset + length);
	}
}
