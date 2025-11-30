package org.logoce.lmf.model.loader;

import org.logoce.lmf.model.lang.LMCorePackage;
import org.logoce.lmf.model.lang.LMObject;
import org.logoce.lmf.model.lang.MetaModel;
import org.logoce.lmf.model.lang.Model;
import org.logoce.lmf.model.lexer.ELMTokenType;
import org.logoce.lmf.model.loader.diagnostic.LmDiagnostic;
import org.logoce.lmf.model.loader.linking.LmModelLinker;
import org.logoce.lmf.model.loader.model.LmDocument;
import org.logoce.lmf.model.loader.parsing.LmTreeReader;
import org.logoce.lmf.model.resource.parsing.PNode;
import org.logoce.lmf.model.util.ModelRegistry;
import org.logoce.lmf.model.util.tree.Tree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * High-level loader for .lm models, designed for tooling integration.
 * <p>
 * It exposes both the parsed tree ({@link PNode}) and the linked {@link Model}
 * along with structured diagnostics and the original source text.
 */
public final class LmLoader
{
	private final ModelRegistry modelRegistry;

	public LmLoader(final ModelRegistry modelRegistry)
	{
		this.modelRegistry = modelRegistry;
	}

	public static LmLoader withEmptyRegistry()
	{
		return new LmLoader(ModelRegistry.empty());
	}

	public LmDocument loadModel(final InputStream inputStream) throws IOException
	{
		final var source = readAll(inputStream);
		return loadModel(source);
	}

	public LmDocument loadModel(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var treeReader = new LmTreeReader();
		final var readResult = treeReader.read(source, diagnostics);

		final List<Tree<PNode>> roots = readResult.roots();
		if (roots.isEmpty())
		{
			return new LmDocument(null, List.copyOf(diagnostics), roots, readResult.source());
		}

		final var effectiveRegistry = isMetaModelRoot(roots)
									  ? ensureLmCore(modelRegistry)
									  : modelRegistry;

		final var linker = new LmModelLinker<PNode>(effectiveRegistry);
		final var linkResult = linker.linkModel(roots, diagnostics, readResult.source());

		return new LmDocument(linkResult.model(), List.copyOf(diagnostics), roots, readResult.source());
	}

	/**
	 * Load arbitrary LMObjects from a single source (not just a Model root).
	 * Mirrors the legacy {@code ResourceUtil.loadObject} behavior but uses the new loader/linker stack.
	 */
	public List<? extends LMObject> loadObjects(final InputStream inputStream) throws IOException
	{
		final var source = readAll(inputStream);
		return loadObjects(source);
	}

	public List<? extends LMObject> loadObjects(final CharSequence source)
	{
		final var diagnostics = new ArrayList<LmDiagnostic>();
		final var treeReader = new LmTreeReader();
		final var readResult = treeReader.read(source, diagnostics);

		final var roots = readResult.roots();
		if (roots.isEmpty())
		{
			return List.of();
		}

		final var linker = new LmModelLinker<PNode>(modelRegistry);
		final var linkResult = linker.linkModelStrict(roots);
		return linkResult.roots();
	}

	/**
	 * Load multiple models with import resolution, similar to {@code ResourceUtil.loadModels}.
	 * <p>
	 * Each root {@code (MetaModel ...)} in the provided streams is treated as a separate model.
	 */
	public List<Model> loadModels(final List<InputStream> inputStreams) throws IOException
	{
		final var treeReader = new LmTreeReader();
		final var parsedModels = new ArrayList<ParsedModel>();

		for (final var in : inputStreams)
		{
			final var source = readAll(in);
			final var diagnostics = new ArrayList<LmDiagnostic>();
			final var readResult = treeReader.read(source, diagnostics);

			if (readResult.roots().isEmpty())
			{
				if (diagnostics.stream().anyMatch(d -> d.severity() == LmDiagnostic.Severity.ERROR))
				{
					final var first = diagnostics.getFirst();
					throw new IllegalArgumentException("Failed to parse model at " +
													   first.line() +
													   ":" +
													   first.column() +
													   " - " +
													   first.message());
				}
				continue;
			}

			for (final var root : readResult.roots())
			{
				parsedModels.add(ParsedModel.from(root, readResult.source()));
			}
		}

		return MultiModelSupport.buildAll(parsedModels, modelRegistry);
	}

	private static ModelRegistry ensureLmCore(final ModelRegistry registry)
	{
		final var existing = registry.getModel(LMCorePackage.MODEL.domain(), LMCorePackage.MODEL.name());
		if (existing != null)
		{
			return registry;
		}

		final var builder = new ModelRegistry.Builder(registry);
		builder.register(LMCorePackage.MODEL);
		return builder.build();
	}

	private static boolean isMetaModelRoot(final List<Tree<PNode>> roots)
	{
		if (roots.isEmpty())
		{
			return false;
		}

		final var tokens = roots.getFirst().data().tokens();
		for (final var token : tokens)
		{
			final String value = token.value();
			if (value == null || value.isBlank() || "(".equals(value))
			{
				continue;
			}
			return "MetaModel".equals(value);
		}
		return false;
	}

	private static String readAll(final InputStream in) throws IOException
	{
		try (final var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)))
		{
			final var sb = new StringBuilder();
			String line;
			boolean first = true;
			while ((line = reader.readLine()) != null)
			{
				if (!first)
				{
					sb.append('\n');
				}
				sb.append(line);
				first = false;
			}
			return sb.toString();
		}
	}

	private record ParsedModel(Tree<PNode> tree, CharSequence source, String qualifiedName, List<String> imports)
	{
		static ParsedModel from(final Tree<PNode> tree, final CharSequence source)
		{
			final var node = tree.data();
			final var imports = resolveImports(node);
			final var domain = resolveDomain(node);
			final var name = resolveName(node);
			final var qualifiedName = domain == null || domain.isBlank() ? name : domain + "." + name;
			return new ParsedModel(tree, source, qualifiedName, imports);
		}

		private static List<String> resolveImports(final PNode node)
		{
			final var it = node.tokens().iterator();
			while (it.hasNext())
			{
				final var token = it.next();
				if (token.type() == ELMTokenType.VALUE_NAME && token.value().equals("imports"))
				{
					if (it.hasNext()) it.next(); // skip ASSIGN

					final var values = new ArrayList<String>();
					while (it.hasNext())
					{
						final var next = it.next();
						if (next.type() == ELMTokenType.VALUE)
						{
							for (final var part : next.value().split(","))
							{
								final var trimmed = part.trim();
								if (!trimmed.isEmpty())
								{
									values.add(trimmed);
								}
							}
						}
						else if (next.type() != ELMTokenType.LIST_SEPARATOR && next.type() != ELMTokenType.WHITE_SPACE)
						{
							break;
						}
					}
					return values;
				}
			}
			return List.of();
		}

		private static String resolveDomain(final PNode node)
		{
			return extractValue(node, "domain");
		}

		private static String resolveName(final PNode node)
		{
			final var name = extractValue(node, "name");
			if (name == null)
			{
				throw new IllegalStateException("Model name should be set");
			}
			return name;
		}

		private static String extractValue(final PNode node, final String property)
		{
			final var it = node.tokens().iterator();
			while (it.hasNext())
			{
				final var token = it.next();
				if (token.type() == ELMTokenType.VALUE_NAME && token.value().equals(property))
				{
					if (it.hasNext()) it.next(); // skip ASSIGN
					if (it.hasNext())
					{
						return it.next().value();
					}
					break;
				}
			}
			return null;
		}
	}

	private static final class MultiModelSupport
	{
		private MultiModelSupport()
		{
		}

		static List<Model> buildAll(final List<ParsedModel> parsedModels, final ModelRegistry baseRegistry)
		{
			if (parsedModels.isEmpty()) return List.of();

			final var remaining = new ArrayList<>(parsedModels);
			final var builtByName = new java.util.HashMap<String, Model>();

			final var registryBuilder = new ModelRegistry.Builder(baseRegistry);
			final var availableNames = new HashSet<String>();
			baseRegistry.models().forEach(model -> availableNames.add(domainName(model)));

			while (!remaining.isEmpty())
			{
				boolean progressed = false;
				final var it = remaining.iterator();
				while (it.hasNext())
				{
					final var pm = it.next();
					if (allImportsAvailable(pm.imports(), availableNames))
					{
						final var linker = new LmModelLinker<PNode>(registryBuilder.build());
						final var diagnostics = new ArrayList<LmDiagnostic>();
						final var linkResult = linker.linkModel(List.of(pm.tree()), diagnostics, pm.source());

						final var model = linkResult.model();
						if (model == null)
						{
							throw new IllegalArgumentException("Input doesn't define a valid model: " +
															   pm.qualifiedName());
						}

						registryBuilder.register(model);
						availableNames.add(domainName(model));
						builtByName.put(pm.qualifiedName(), model);
						it.remove();
						progressed = true;
					}
				}

				if (!progressed)
				{
					throw new IllegalStateException("Cannot resolve all imports between provided models");
				}
			}

			// Preserve the original parsedModels order in the returned list, as the legacy
			// MultiModelLoader did via its BuildingModel list.
			final var ordered = new ArrayList<Model>(parsedModels.size());
			for (final var pm : parsedModels)
			{
				final var model = builtByName.get(pm.qualifiedName());
				if (model == null)
				{
					throw new IllegalStateException("No built model for " + pm.qualifiedName());
				}
				ordered.add(model);
			}
			return List.copyOf(ordered);
		}

		private static boolean allImportsAvailable(final List<String> imports, final Set<String> available)
		{
			for (final var imp : imports)
			{
				if (!available.contains(imp))
				{
					return false;
				}
			}
			return true;
		}

		private static String domainName(final Model model)
		{
			if (model instanceof MetaModel mm)
			{
				return mm.domain() + "." + mm.name();
			}
			else
			{
				return model.name();
			}
		}
	}
}
