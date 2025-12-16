package org.logoce.lmf.core.api.loader;

import org.logoce.lmf.core.api.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.core.api.loader.model.LmDocument;
import org.logoce.lmf.core.api.loader.parsing.LmTreeReader;
import org.logoce.lmf.core.api.loader.parsing.ModelHeaderUtil;
import org.logoce.lmf.core.api.text.syntax.PNode;
import org.logoce.lmf.core.api.model.ModelRegistry;
import org.logoce.lmf.core.util.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Workspace-oriented helpers for loading multiple meta-models together.
 * <p>
 * This mirrors the multi-model behaviour used by the LSP: .lm files are parsed
 * first, {@code MetaModel} roots are detected from the header only, and then a
 * {@link ModelRegistry} is built for all detected meta-models in a single pass.
 */
public final class LmWorkspace
{
	private LmWorkspace()
	{
	}

	/**
	 * Parse all provided files, keep only those whose root is a {@code MetaModel},
	 * and build a {@link ModelRegistry} for them using {@link LmLoader#buildRegistry(List, ModelRegistry)}.
	 * <p>
	 * Files that cannot be parsed or that do not contain a {@code MetaModel}
	 * root are ignored. Parsing diagnostics are preserved in the returned
	 * {@link LmDocument} instances but linking is deferred to the registry
	 * build step.
	 *
	 * @param modelFiles   candidate .lm files (typically all .lm under a source set)
	 * @param baseRegistry base registry to extend (for example {@link ModelRegistry#empty()})
	 * @return a workspace snapshot carrying the meta-model files, their parsed documents,
	 * and the resulting {@link ModelRegistry}
	 * @throws IOException if any file cannot be read
	 */
	public static MetaModelWorkspace loadMetaModels(final List<File> modelFiles, final ModelRegistry baseRegistry)
		throws IOException
	{
		final var reader = new LmTreeReader();
		final List<File> metaModelFiles = new ArrayList<>();
		final List<LmDocument> documents = new ArrayList<>();

		for (final var file : modelFiles)
		{
			final var source = readFile(file);
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var readResult = reader.read(source, diagnostics);
			final List<Tree<PNode>> roots = readResult.roots();

			if (roots.isEmpty())
			{
				continue;
			}
			if (ModelHeaderUtil.isMetaModelRoot(roots) == false)
			{
				continue;
			}

			final var document = new LmDocument(null,
												List.copyOf(diagnostics),
												List.copyOf(roots),
												readResult.source(),
												List.of());

			metaModelFiles.add(file);
			documents.add(document);
		}

		if (documents.isEmpty())
		{
			return new MetaModelWorkspace(List.of(), List.of(), baseRegistry);
		}

		final var registry = LmLoader.buildRegistry(documents, baseRegistry);
		return new MetaModelWorkspace(List.copyOf(metaModelFiles), List.copyOf(documents), registry);
	}

	private static CharSequence readFile(final File file) throws IOException
	{
		final var path = file.toPath();
		final byte[] bytes = Files.readAllBytes(path);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public record MetaModelWorkspace(List<File> files, List<LmDocument> documents, ModelRegistry registry)
	{
	}
}
