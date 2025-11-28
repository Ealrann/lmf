package org.logoce.lmf.model.resource;

import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.loader.LmLoader;
import org.logoce.lmf.model.loader.model.LmDocument;
import org.logoce.lmf.model.resource.parsing.PTreeReader;
import org.logoce.lmf.model.resource.parsing.ParseDiagnostic;
import org.logoce.lmf.model.resource.transform.PModelLinker;
import org.logoce.lmf.model.resource.transform.multi.MultiModelLoader;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.resource.linking.exception.LinkException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ResourceUtil
{
	public static List<? extends LMObject> loadObject(final InputStream inputStream, final ModelRegistry modelRegistry)
	{
		try
		{
			final var loader = new LmLoader(modelRegistry);
			return loader.loadObjects(inputStream);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read LM model", e);
		}
	}

	public static Model loadModel(final InputStream inputStream, final ModelRegistry modelRegistry)
	{
		try
		{
			final var loader = new LmLoader(modelRegistry);
			final LmDocument doc = loader.loadModel(inputStream);
			final var model = doc.model();
			if (model instanceof Model m)
			{
				return m;
			}
			throw new IllegalArgumentException("This input doesn't define a valid model. Use loadObject() instead.");
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read LM model", e);
		}
	}

	public static List<Model> loadModels(final List<InputStream> inputStreams, final ModelRegistry modelRegistry)
	{
		try
		{
			final var loader = new LmLoader(modelRegistry);
			return loader.loadModels(inputStreams);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to read LM models", e);
		}
	}

	public static ParseResult loadModelWithDiagnostics(final InputStream inputStream, final ModelRegistry modelRegistry)
	{
		final var diagnostics = new ArrayList<ParseDiagnostic>();
		final var ptreeReader = new PTreeReader();
		final var readResult = ptreeReader.readWithDiagnostics(inputStream, diagnostics);
		final var roots = readResult.model();
		diagnostics.addAll(readResult.diagnostics());
		if (roots.isEmpty()) {
			return new ParseResult(null, diagnostics, roots, readResult.source());
		}
		final var linker = new PModelLinker<>(modelRegistry);
		try {
			final var linked = linker.link(roots, (pNode, e) -> {
				final var span = spanOf(pNode, readResult.source());
				diagnostics.add(new ParseDiagnostic(span.line(), span.column(), span.length(), span.offset(),
													ParseDiagnostic.Severity.ERROR,
													e.getMessage() == null ? "Link error" : e.getMessage()));
			});
			final var built = linked.build().get(0);
			if (built instanceof Model model) return new ParseResult(model, diagnostics, roots, readResult.source());
		} catch (LinkException e) {
			final var span = spanOf(e.pNode, readResult.source());
			diagnostics.add(new ParseDiagnostic(span.line(), span.column(), span.length(), span.offset(), ParseDiagnostic.Severity.ERROR, e.getMessage() == null ? "Link error" : e.getMessage()));
		} catch (Exception e) {
			diagnostics.add(new ParseDiagnostic(1, 1, 1, 0, ParseDiagnostic.Severity.ERROR, e.getMessage() == null ? "Link error" : e.getMessage()));
		}
		return new ParseResult(null, diagnostics, roots, readResult.source());
	}

	public record ParseResult(Model model, List<ParseDiagnostic> diagnostics, List<Tree<PNode>> roots, CharSequence source) {}

	private record Span(int line, int column, int length, int offset) {}

	private static Span spanOf(PNode node, CharSequence source) {
		return node.tokens().stream()
			.findFirst()
			.map(tok -> {
				final int offset = tok.offset();
				final int length = Math.max(1, tok.length());
				int line = 1;
				int col = 1;
				for (int i = 0; i < offset && i < source.length(); i++) {
					if (source.charAt(i) == '\n') {
						line++;
						col = 1;
					} else {
						col++;
					}
				}
				return new Span(line, col, length, offset);
			})
			.orElse(new Span(1, 1, 1, 0));
	}
}
